package com.huafu.crm.wecom.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.regex.Pattern;

@Component
public class WeComXmlParser {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@[^\\s　:：,，]+[\\s　:：,，]*");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ParsedMessage parse(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new ParsedMessage("", "", "", "", "");
        }
        if (!raw.trim().startsWith("<")) {
            return parseJsonOrText(raw);
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(raw)));
            String fromUser = textOf(document, "FromUserName");
            String toUser = textOf(document, "ToUserName");
            String msgType = textOf(document, "MsgType");
            String msgId = textOf(document, "MsgId");
            String content = textOf(document, "Content");
            if (!StringUtils.hasText(content)) {
                content = textOf(document, "Recognition");
            }
            if (!StringUtils.hasText(content)) {
                content = textOf(document, "EventKey");
            }
            return new ParsedMessage(fromUser, toUser, msgType, msgId, stripMentions(content));
        } catch (Exception ex) {
            return new ParsedMessage("", "", "text", "", stripMentions(raw));
        }
    }

    private ParsedMessage parseJsonOrText(String raw) {
        String value = raw.trim();
        if (!value.startsWith("{")) {
            return new ParsedMessage("", "", "text", "", stripMentions(raw));
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(value);
            JsonNode data = root.path("data");
            JsonNode body = root.path("body");
            JsonNode source = body.isObject() ? body : (data.isObject() ? data : root);
            String content = firstText(source, "content", "text", "message", "msg", "body", "summary");
            if (!StringUtils.hasText(content)) {
                content = firstNestedText(source, "text", "content");
            }
            if (!StringUtils.hasText(content)) {
                content = firstNestedText(source, "message", "content");
            }
            if (!StringUtils.hasText(content)) {
                content = firstNestedText(source, "voice", "content");
            }
            if (!StringUtils.hasText(content)) {
                content = firstText(root, "content", "text", "message", "msg", "body", "summary");
            }
            String fromUser = firstText(source, "from_user", "fromUser", "sender", "sender_id", "senderId", "user_id", "userId");
            if (!StringUtils.hasText(fromUser)) {
                fromUser = firstNestedText(source, "from", "userid", "user_id", "id", "name");
            }
            String toUser = firstText(source, "chatid", "chat_id", "chatId", "room_id", "roomId", "room_name", "roomName", "conversation_id", "conversationId", "to_user", "toUser");
            String msgType = firstText(source, "msgtype", "msg_type", "msgType", "type", "message_type", "messageType");
            String msgId = firstText(source, "msgid", "msg_id", "msgId", "message_id", "messageId", "id");
            return new ParsedMessage(fromUser, toUser, StringUtils.hasText(msgType) ? msgType : "text", msgId, stripMentions(content));
        } catch (Exception ignored) {
            return new ParsedMessage("", "", "text", "", stripMentions(raw));
        }
    }

    private String firstText(JsonNode node, String... fieldNames) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        for (String fieldName : fieldNames) {
            JsonNode value = node.path(fieldName);
            if (value.isTextual() && StringUtils.hasText(value.asText())) {
                return value.asText();
            }
            if (value.isNumber() || value.isBoolean()) {
                return value.asText();
            }
        }
        return "";
    }

    private String firstNestedText(JsonNode node, String objectName, String... fieldNames) {
        JsonNode nested = node == null ? null : node.path(objectName);
        if (nested != null && nested.isTextual()) {
            return nested.asText();
        }
        return firstText(nested, fieldNames);
    }

    public String stripMentions(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        return MENTION_PATTERN.matcher(content).replaceAll("").trim();
    }

    private String textOf(Document document, String tagName) {
        var nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() == 0 || nodes.item(0) == null) {
            return "";
        }
        return nodes.item(0).getTextContent();
    }

    public record ParsedMessage(String fromUser, String toUser, String msgType, String msgId, String content) {}
}
