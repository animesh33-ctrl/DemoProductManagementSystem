package com.security.xss;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.owasp.validator.html.*;

import java.util.Objects;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private static final Policy policy;

    static {
        try {
            policy = Policy.getInstance(
                    Objects.requireNonNull(XssRequestWrapper.class
                            .getResourceAsStream("/antisamy-slashdot.xml"))
            );
        } catch (PolicyException e) {
            throw new RuntimeException("Failed to load AntiSamy policy", e);
        }
    }

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return sanitize(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = sanitize(values[i]);
        }
        return sanitized;
    }

    @Override
    public String getHeader(String name) {
        return sanitize(super.getHeader(name));
    }

    private String sanitize(String value) {
        if (value == null) return null;
        try {
            AntiSamy antiSamy = new AntiSamy();
            CleanResults results = antiSamy.scan(value, policy);
            return results.getCleanHTML();
        } catch (Exception e) {
            return ""; // if scan fails — return empty (safe default)
        }
    }
}