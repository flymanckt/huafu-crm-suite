package com.huafu.crm.common.web;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

/**
 * 全局异常处理
 * 约定：所有HTTP状态码返回200，由业务错误码code字段区分成功/失败
 * 异常信息全部中文友好，避免直接暴露技术细节
 * 注意：此类仅在Servlet容器中加载，Gateway(WebFlux)不使用此类
 */
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException ex) {
        log.warn("[业务异常] code={} message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.ok(Result.fail(Integer.parseInt(ex.getCode()), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError() != null
            ? ex.getBindingResult().getFieldError().getDefaultMessage()
            : "参数校验失败";
        log.warn("[参数校验] {}", msg);
        return ResponseEntity.ok(Result.fail(400, "参数校验失败：" + msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().iterator().hasNext()
            ? ex.getConstraintViolations().iterator().next().getMessage()
            : ex.getMessage();
        log.warn("[约束校验] {}", msg);
        return ResponseEntity.ok(Result.fail(400, "参数校验失败：" + msg));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoSuchElementException ex) {
        log.warn("[资源不存在] {}", ex.getMessage());
        return ResponseEntity.ok(Result.fail(404, "请求的数据不存在，请刷新后重试"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArg(IllegalArgumentException ex) {
        log.warn("[非法参数] {}", ex.getMessage());
        return ResponseEntity.ok(Result.fail(400, "请求参数有误：" + ex.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Void>> handleNPE(NullPointerException ex, HttpServletRequest request) {
        log.error("[空指针异常] path={}", request.getRequestURI(), ex);
        return ResponseEntity.ok(Result.fail(500, "系统内部错误，请联系管理员"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handle(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String msg = ex.getMessage();

        // 友好的中文提示，避免暴露 "No static resource" 等技术词汇
        if (msg == null || msg.isBlank()) {
            msg = "系统内部错误";
        } else if (msg.contains("No static resource")) {
            msg = "请求的接口不存在，请检查URL是否正确";
        } else if (msg.contains("Access Denied") || msg.contains("Access is Denied")) {
            msg = "无权访问该资源";
        } else if (msg.contains("Connection refused")) {
            msg = "后端服务连接失败，请稍后重试";
        } else if (msg.length() > 100) {
            // 截断过长的异常信息，避免前端展示问题
            msg = msg.substring(0, 100) + "...（详情见服务器日志）";
        }

        log.error("[未处理异常] path={} type={} message={}", path, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return ResponseEntity.ok(Result.fail(500, msg));
    }
}
