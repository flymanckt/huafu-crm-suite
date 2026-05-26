package com.huafu.crm.wecom.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.wecom.dispatcher.MessageDispatcher;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class WeComAiBotWebSocketClient implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(WeComAiBotWebSocketClient.class);
    private static final String DEFAULT_WS_URL = "wss://openws.work.weixin.qq.com";
    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;
    private static final long HEARTBEAT_TIMEOUT_SECONDS = 90;

    private final JdbcTemplate jdbcTemplate;
    private final MessageDispatcher dispatcher;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "wecom-aibot-ws");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicReference<WebSocket> webSocket = new AtomicReference<>();
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private final AtomicReference<String> subscribeReqId = new AtomicReference<>();
    private final AtomicReference<Instant> lastInboundAt = new AtomicReference<>(Instant.EPOCH);
    private final String deviceId = UUID.randomUUID().toString().replace("-", "");

    public WeComAiBotWebSocketClient(JdbcTemplate jdbcTemplate, MessageDispatcher dispatcher, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.dispatcher = dispatcher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isAiBotModeEnabled()) {
            log.info("WeCom AI Bot websocket disabled by config");
            return;
        }
        connectSoon(0);
        executor.scheduleWithFixedDelay(this::sendHeartbeat, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        stopping.set(true);
        WebSocket socket = webSocket.getAndSet(null);
        if (socket != null) {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
        }
        executor.shutdownNow();
    }

    private boolean isAiBotModeEnabled() {
        boolean receiveEnabled = Boolean.parseBoolean(configValue("wecom.receive_enabled", "true"));
        String mode = configValue("wecom.receive_mode", "WECOM_AI_BOT");
        return receiveEnabled && ("WECOM_AI_BOT".equalsIgnoreCase(mode) || "AI_BOT_WEBSOCKET".equalsIgnoreCase(mode));
    }

    private void connectSoon(long delaySeconds) {
        if (stopping.get()) {
            return;
        }
        executor.schedule(() -> {
            reconnecting.set(false);
            connect();
        }, delaySeconds, TimeUnit.SECONDS);
    }

    private void connect() {
        if (stopping.get()) {
            return;
        }
        String botId = configValue("wecom.cli_bot_id", "");
        String secret = configValue("wecom.cli_bot_secret", "");
        String wsUrl = configValue("wecom.aibot_websocket_url", DEFAULT_WS_URL);
        if (!StringUtils.hasText(botId) || !StringUtils.hasText(secret)) {
            log.warn("WeCom AI Bot websocket not started: Bot ID/Secret is empty");
            return;
        }
        try {
            log.info("Connecting WeCom AI Bot websocket: {}", wsUrl);
            httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .buildAsync(URI.create(wsUrl), new Listener())
                    .thenAccept(socket -> {
                        webSocket.set(socket);
                        lastInboundAt.set(Instant.now());
                        sendSubscribe(socket, botId, secret);
                    })
                    .exceptionally(ex -> {
                        log.warn("WeCom AI Bot websocket connect failed: {}", ex.getMessage());
                        connectSoon(10);
                        return null;
                    });
        } catch (Exception ex) {
            log.warn("WeCom AI Bot websocket connect error: {}", ex.getMessage());
            connectSoon(10);
        }
    }

    private void sendSubscribe(WebSocket socket, String botId, String secret) {
        String reqId = reqId("subscribe");
        subscribeReqId.set(reqId);
        sendJson(socket, Map.of(
                "cmd", "aibot_subscribe",
                "headers", Map.of("req_id", reqId),
                "body", Map.of(
                        "bot_id", botId,
                        "secret", secret,
                        "device_id", deviceId
                )
        ));
        executor.schedule(() -> {
            if (reqId.equals(subscribeReqId.get()) && !stopping.get()) {
                log.warn("Timed out waiting for WeCom AI Bot subscribe acknowledgement");
            }
        }, 20, TimeUnit.SECONDS);
    }

    private void sendHeartbeat() {
        WebSocket socket = webSocket.get();
        if (socket == null || stopping.get()) {
            return;
        }
        long idleSeconds = Duration.between(lastInboundAt.get(), Instant.now()).getSeconds();
        if (idleSeconds > HEARTBEAT_TIMEOUT_SECONDS) {
            log.warn("WeCom AI Bot websocket heartbeat timed out: idleSeconds={}, reconnecting", idleSeconds);
            reconnect(socket, 3);
            return;
        }
        sendJson(socket, Map.of(
                "cmd", "ping",
                "headers", Map.of("req_id", reqId("ping")),
                "body", Map.of()
        ));
    }

    private void reconnect(WebSocket socket, long delaySeconds) {
        if (stopping.get() || !reconnecting.compareAndSet(false, true)) {
            return;
        }
        webSocket.compareAndSet(socket, null);
        try {
            socket.abort();
        } catch (Exception ignored) {
        }
        connectSoon(delaySeconds);
    }

    private void sendJson(WebSocket socket, Object payload) {
        try {
            socket.sendText(objectMapper.writeValueAsString(payload), true);
        } catch (Exception ex) {
            log.debug("Failed to send WeCom websocket payload: {}", ex.getMessage());
        }
    }

    private String reqId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    private String configValue(String key, String defaultValue) {
        try {
            String value = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM sys_config WHERE config_key = ? LIMIT 1",
                    String.class,
                    key
            );
            return StringUtils.hasText(value) ? value : defaultValue;
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private class Listener implements WebSocket.Listener {
        private final StringBuilder textBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("WeCom AI Bot websocket opened");
            lastInboundAt.set(Instant.now());
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (!last) {
                return WebSocket.Listener.super.onText(socket, data, false);
            }
            String payload = textBuffer.toString();
            textBuffer.setLength(0);
            lastInboundAt.set(Instant.now());
            handlePayload(payload);
            return WebSocket.Listener.super.onText(socket, data, true);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket socket, int statusCode, String reason) {
            log.warn("WeCom AI Bot websocket closed: status={}, reason={}", statusCode, reason);
            webSocket.compareAndSet(socket, null);
            if (!stopping.get()) {
                connectSoon(10);
            }
            return WebSocket.Listener.super.onClose(socket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket socket, Throwable error) {
            log.warn("WeCom AI Bot websocket error: {}", error.getMessage());
            webSocket.compareAndSet(socket, null);
            if (!stopping.get()) {
                connectSoon(10);
            }
            WebSocket.Listener.super.onError(socket, error);
        }

        private void handlePayload(String raw) {
            try {
                JsonNode root = objectMapper.readTree(raw);
                String cmd = root.path("cmd").asText("");
                if ("aibot_msg_callback".equals(cmd) || "aibot_callback".equals(cmd)) {
                    log.info("Received WeCom AI Bot callback: msgId={}, from={}",
                            root.path("body").path("msgid").asText(""),
                            root.path("body").path("from").path("userid").asText(""));
                    dispatcher.dispatch(raw);
                    return;
                }
                if ("aibot_subscribe".equals(cmd)) {
                    handleSubscribeAck(root);
                    return;
                }
                if (subscribeReqId.get() != null && subscribeReqId.get().equals(payloadReqId(root))) {
                    handleSubscribeAck(root);
                    return;
                }
                if (!"ping".equals(cmd) && !"aibot_event_callback".equals(cmd)) {
                    log.debug("Ignoring WeCom websocket payload cmd={}", cmd);
                }
            } catch (Exception ex) {
                log.debug("Failed to handle WeCom websocket payload: {}", ex.getMessage());
            }
        }

        private void handleSubscribeAck(JsonNode root) {
            JsonNode body = root.path("body");
            int errcode = body.path("errcode").asInt(root.path("errcode").asInt(0));
            String errmsg = body.path("errmsg").asText(root.path("errmsg").asText(""));
            if (errcode == 0) {
                subscribeReqId.set(null);
                log.info("WeCom AI Bot websocket subscribed");
            } else {
                log.warn("WeCom AI Bot subscribe failed: errcode={}, errmsg={}", errcode, errmsg);
            }
        }

        private String payloadReqId(JsonNode root) {
            return root.path("headers").path("req_id").asText("");
        }
    }
}
