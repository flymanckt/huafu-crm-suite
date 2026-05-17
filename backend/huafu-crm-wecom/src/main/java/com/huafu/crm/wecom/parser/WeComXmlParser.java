package com.huafu.crm.wecom.parser;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@Component
public class WeComXmlParser {
    public ParsedMessage parse(String xml) {
        if (!StringUtils.hasText(xml)) {
            return new ParsedMessage("", "", "", "", "");
        }
        if (!xml.trim().startsWith("<")) {
            return new ParsedMessage("", "", "text", "", xml);
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
            return new ParsedMessage(fromUser, toUser, msgType, msgId, content);
        } catch (Exception ex) {
            return new ParsedMessage("", "", "text", "", xml);
        }
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
