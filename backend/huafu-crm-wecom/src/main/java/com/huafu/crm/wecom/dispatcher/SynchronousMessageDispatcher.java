package com.huafu.crm.wecom.dispatcher;

import com.huafu.crm.wecom.processor.WeComMessageProcessor;
import org.springframework.stereotype.Component;

@Component
public class SynchronousMessageDispatcher implements MessageDispatcher {
    private final WeComMessageProcessor processor;

    public SynchronousMessageDispatcher(WeComMessageProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void dispatch(String rawXml) {
        processor.process(rawXml);
    }
}
