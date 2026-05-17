package com.huafu.crm.common.util;

import com.huafu.crm.common.exception.BizException;
import java.util.Set;
import org.springframework.util.StringUtils;

public final class InputSanitizer {
    private static final Set<String> XSS_TAG_SET = Set.of(
        "script", "iframe", "object", "embed", "link", "style", "base", "svg", "math", "body"
    );
    private static final Set<String> XSS_ATTR_SET = Set.of(
        "onerror", "onload", "onclick", "onmouseover", "onfocus", "onblur", "onchange", "onsubmit",
        "oninput", "onkeydown", "onkeyup", "onkeypress", "ondblclick", "oncontextmenu"
    );

    private InputSanitizer() {
    }

    public static String safeText(String input) {
        if (input == null) return null;
        return escapeHtml(rejectUnsafeHtml(input));
    }

    public static String rejectUnsafeHtml(String input) {
        if (!StringUtils.hasText(input)) return input;
        String lower = input.toLowerCase();
        for (String tag : XSS_TAG_SET) {
            if (lower.contains("<" + tag) || lower.contains("</" + tag)) {
                throw new BizException(1004, "输入内容包含非法字符，禁止使用HTML标签");
            }
        }
        for (String attr : XSS_ATTR_SET) {
            if (lower.contains(attr + "=")) {
                throw new BizException(1004, "输入内容包含非法字符，禁止使用事件属性");
            }
        }
        return input;
    }

    private static String escapeHtml(String input) {
        if (input == null || input.isBlank()) return input;
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }
}
