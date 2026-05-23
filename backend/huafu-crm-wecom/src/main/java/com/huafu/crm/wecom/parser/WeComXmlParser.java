package com.huafu.crm.wecom.parser;

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

    public ParsedMessage parse(String xml) {
        if (!StringUtils.hasText(xml)) {
            return new ParsedMessage("", "", "", "", "");
        }
        if (!xml.trim().startsWith("<")) {
            return new ParsedMessage("", "", "text", "", stripMentions(extractJsonText(xml)));
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
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
            return new ParsedMessage("", "", "text", "", stripMentions(xml));
        }
    }

    private String extractJsonText(String raw) {
        String value = raw.trim();
        if (!value.startsWith("{")) {
            return raw;
        }
        String content = extractJsonString(value, "content");
        if (StringUtils.hasText(content)) {
            return content;
        }
        String text = extractJsonString(value, "text");
        return StringUtils.hasText(text) ? text : raw;
    }

    private String extractJsonString(String json, String key) {
        var matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(json);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1)
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
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
