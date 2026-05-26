package com.huafu.crm.wecom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.wecom.dispatcher.MessageDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WeComReceiveControllerTest {
    @Test
    void receivePostShouldDispatchAndReturnSuccessQuickly() throws Exception {
        MessageDispatcher dispatcher = mock(MessageDispatcher.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WeComReceiveController(dispatcher, new ObjectMapper())).build();
        String xml = "<xml><FromUserName>u1</FromUserName><Content>hello</Content></xml>";

        mockMvc.perform(post("/api/wecom/callback").content(xml))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        verify(dispatcher).dispatch(xml);
    }

    @Test
    void receivePostShouldSplitCliMessageArray() throws Exception {
        MessageDispatcher dispatcher = mock(MessageDispatcher.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WeComReceiveController(dispatcher, new ObjectMapper())).build();
        String payload = "{\"messages\":[{\"msg_id\":\"1\",\"content\":\"daily A\"},{\"msg_id\":\"2\",\"content\":\"daily B\"}]}";

        mockMvc.perform(post("/api/wecom/callback").content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        verify(dispatcher).dispatch("{\"msg_id\":\"1\",\"content\":\"daily A\"}");
        verify(dispatcher).dispatch("{\"msg_id\":\"2\",\"content\":\"daily B\"}");
    }
}
