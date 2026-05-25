package com.bigdata.admin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Request/Response logging filter
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final int MAX_PAYLOAD_LENGTH = 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        String method = request.getMethod();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // Log request details
            if (status >= 400) {
                logRequestResponse(requestWrapper, responseWrapper, uri, method, status, duration);
            } else {
                log.debug("{} {} - Status: {} - Duration: {}ms", method, uri, status, duration);
            }

            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestResponse(ContentCachingRequestWrapper requestWrapper,
                                    ContentCachingResponseWrapper responseWrapper,
                                    String uri, String method, int status, long duration) {
        String requestBody = getPayload(requestWrapper.getContentAsByteArray());
        String responseBody = getPayload(responseWrapper.getContentAsByteArray());

        log.warn("Request: {} {} | Status: {} | Duration: {}ms | Request: {} | Response: {}",
                method, uri, status, duration,
                truncate(requestBody), truncate(responseBody));
    }

    private String getPayload(byte[] buf) {
        if (buf == null || buf.length == 0) {
            return "";
        }
        return new String(buf, StandardCharsets.UTF_8);
    }

    private String truncate(String payload) {
        if (payload.length() > MAX_PAYLOAD_LENGTH) {
            return payload.substring(0, MAX_PAYLOAD_LENGTH) + "...";
        }
        return payload;
    }
}
