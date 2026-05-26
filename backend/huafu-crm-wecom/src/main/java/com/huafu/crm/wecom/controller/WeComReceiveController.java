package com.huafu.crm.wecom.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.wecom.dispatcher.MessageDispatcher;
import org.springframework.web.bind.annotation.*;

@RestController
public class WeComReceiveController {
    private final MessageDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    public WeComReceiveController(MessageDispatcher dispatcher, ObjectMapper objectMapper) {
        this.dispatcher = dispatcher;
        this.objectMapper = objectMapper;
    }

    @GetMapping({"/api/wecom/callback", "/wecom/receive", "/wecom/callback"})
    public String verify(@RequestParam(required = false) String echostr) {
        return echostr == null ? "ok" : echostr;
    }

    /** Returns immediately after crossing the dispatch boundary; MQ adapter can replace the Phase 0 synchronous dispatcher. */
    @PostMapping({"/api/wecom/callback", "/wecom/receive", "/wecom/callback"})
    public String receive(@RequestBody String payload) {
        dispatchPayload(payload);
        return "success";
    }

    @PostMapping("/api/wecom/callback/mock")
    public Result<Void> mock(@RequestBody String payload) {
        dispatchPayload(payload);
        return Result.ok();
    }

    private void dispatchPayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            dispatcher.dispatch(payload);
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root.isArray()) {
                root.forEach(node -> dispatcher.dispatch(node.toString()));
                return;
            }
            JsonNode messages = firstArray(root, "messages", "messageList", "msg_list", "items");
            if (messages != null) {
                messages.forEach(node -> dispatcher.dispatch(node.toString()));
                return;
            }
            JsonNode data = root.path("data");
            if (data.isArray()) {
                data.forEach(node -> dispatcher.dispatch(node.toString()));
                return;
            }
            JsonNode dataMessages = firstArray(data, "messages", "messageList", "msg_list", "items");
            if (dataMessages != null) {
                dataMessages.forEach(node -> dispatcher.dispatch(node.toString()));
                return;
            }
        } catch (Exception ignored) {
            // XML, plain text, or already decrypted callback bodies are handled by the parser downstream.
        }
        dispatcher.dispatch(payload);
    }

    private JsonNode firstArray(JsonNode root, String... fieldNames) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode value = root.path(fieldName);
            if (value.isArray()) {
                return value;
            }
        }
        return null;
    }
}
