package com.bigdata.admin.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Security Headers Filter
 * Adds security headers to all HTTP responses for defense-in-depth
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@WebFilter(urlPatterns = "/*")
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Prevent MIME type sniffing
        httpResponse.setHeader("X-Content-Type-Options", WebConfig.SecurityHeaders.X_CONTENT_TYPE_OPTIONS);

        // Prevent clickjacking
        httpResponse.setHeader("X-Frame-Options", WebConfig.SecurityHeaders.X_FRAME_OPTIONS);

        // Enable XSS protection
        httpResponse.setHeader("X-XSS-Protection", WebConfig.SecurityHeaders.X_XSS_PROTECTION);

        // Enforce HTTPS (only if the request is already secure)
        if (request.isSecure()) {
            httpResponse.setHeader("Strict-Transport-Security", WebConfig.SecurityHeaders.STRICT_TRANSPORT_SECURITY);
        }

        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy", WebConfig.SecurityHeaders.CONTENT_SECURITY_POLICY);

        // Referrer Policy
        httpResponse.setHeader("Referrer-Policy", WebConfig.SecurityHeaders.REFERRER_POLICY);

        // Permissions Policy
        httpResponse.setHeader("Permissions-Policy", WebConfig.SecurityHeaders.PERMISSIONS_POLICY);

        // Cache control for API responses
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");

        // Pragma header for HTTP/1.0 clients
        httpResponse.setHeader("Pragma", "no-cache");

        log.debug("Security headers applied to response");

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("Security Headers Filter initialized");
    }

    @Override
    public void destroy() {
        log.info("Security Headers Filter destroyed");
    }
}
