package com.huafu.crm.wecom.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeComXmlParserTest {
    private final WeComXmlParser parser = new WeComXmlParser();

    @Test
    void parseShouldExtractTextMessageFields() {
        String xml = """
                <xml>
                  <ToUserName><![CDATA[toUser]]></ToUserName>
                  <FromUserName><![CDATA[fromUser]]></FromUserName>
                  <CreateTime>1700000000</CreateTime>
                  <MsgType><![CDATA[text]]></MsgType>
                  <Content><![CDATA[今日日报：新增商机1个]]></Content>
                  <MsgId>123456</MsgId>
                </xml>
                """;

        WeComXmlParser.ParsedMessage message = parser.parse(xml);

        assertThat(message.fromUser()).isEqualTo("fromUser");
        assertThat(message.content()).isEqualTo("今日日报：新增商机1个");
    }

    @Test
    void parseShouldReturnRawContentForInvalidXml() {
        WeComXmlParser.ParsedMessage message = parser.parse("not xml");

        assertThat(message.fromUser()).isEmpty();
        assertThat(message.content()).isEqualTo("not xml");
    }

    @Test
    void parseShouldStripMentionPrefixAndReadJsonContent() {
        WeComXmlParser.ParsedMessage message = parser.parse("{\"content\":\"@CRM机器人 今日日报：拜访客户A\"}");

        assertThat(message.msgType()).isEqualTo("text");
        assertThat(message.content()).isEqualTo("今日日报：拜访客户A");
    }
}
