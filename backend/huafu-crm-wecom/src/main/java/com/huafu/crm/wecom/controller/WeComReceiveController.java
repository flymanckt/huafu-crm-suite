package com.huafu.crm.wecom.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.wecom.dispatcher.MessageDispatcher;
import org.springframework.web.bind.annotation.*;

@RestController
public class WeComReceiveController {
    private final MessageDispatcher dispatcher;

    public WeComReceiveController(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @GetMapping({"/api/wecom/callback", "/wecom/receive"})
    public String verify(@RequestParam(required = false) String echostr) {
        return echostr == null ? "ok" : echostr;
    }

    /** Returns immediately after crossing the dispatch boundary; MQ adapter can replace the Phase 0 synchronous dispatcher. */
    @PostMapping({"/api/wecom/callback", "/wecom/receive"})
    public String receive(@RequestBody String xml) {
        dispatcher.dispatch(xml);
        return "success";
    }

    @PostMapping("/api/wecom/callback/mock")
    public Result<Void> mock(@RequestBody String xml) {
        dispatcher.dispatch(xml);
        return Result.ok();
    }
}
