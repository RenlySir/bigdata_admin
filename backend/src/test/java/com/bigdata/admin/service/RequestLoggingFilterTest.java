package com.bigdata.admin.service;

import com.bigdata.admin.config.RequestLoggingFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestLoggingFilterTest {

    @Test
    void sanitizePayload_ShouldMaskSensitiveValuesBeforeLogging() {
        RequestLoggingFilter filter = new RequestLoggingFilter();

        String sanitized = filter.sanitizePayload("{\"username\":\"admin\",\"password\":\"Secret123\",\"token\":\"abc.def.ghi\"}");

        assertFalse(sanitized.contains("Secret123"));
        assertFalse(sanitized.contains("abc.def.ghi"));
        assertTrue(sanitized.contains("***"));
    }
}
