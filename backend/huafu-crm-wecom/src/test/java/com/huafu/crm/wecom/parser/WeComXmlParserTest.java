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

    @Test
    void parseShouldReadOfficialAiBotCallbackPayload() {
        String payload = """
                {
                  "cmd": "aibot_msg_callback",
                  "headers": {"req_id": "req-1"},
                  "body": {
                    "msgid": "msg-1",
                    "chatid": "group-1",
                    "chattype": "group",
                    "from": {"userid": "user-1"},
                    "msgtype": "text",
                    "text": {"content": "@CRM机器人 今日日报：拜访客户B"}
                  }
                }
                """;

        WeComXmlParser.ParsedMessage message = parser.parse(payload);

        assertThat(message.fromUser()).isEqualTo("user-1");
        assertThat(message.toUser()).isEqualTo("group-1");
        assertThat(message.msgType()).isEqualTo("text");
        assertThat(message.msgId()).isEqualTo("msg-1");
        assertThat(message.content()).isEqualTo("今日日报：拜访客户B");
    }
}
