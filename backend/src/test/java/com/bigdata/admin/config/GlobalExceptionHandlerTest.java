package com.bigdata.admin.config;

import com.bigdata.admin.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgumentException_WhenCalled_ShouldReturnErrorResult() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");

        Result<Void> result = globalExceptionHandler.handleIllegalArgumentException(ex);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("Invalid argument provided", result.getMessage());
    }

    @Test
    void handleGenericException_WhenCalled_ShouldReturnErrorResult() {
        Exception ex = new Exception("Unexpected error");

        Result<Void> result = globalExceptionHandler.handleGenericException(ex);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("Internal server error"));
    }

    @Test
    void handleDataIntegrityViolationException_WhenCalled_ShouldReturnErrorResult() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation");

        Result<Void> result = globalExceptionHandler.handleDataIntegrityViolationException(ex);

        assertNotNull(result);
        assertEquals(409, result.getCode());
        assertTrue(result.getMessage().contains("Data integrity"));
    }

    @Test
    void globalExceptionHandler_ShouldCreateInstance() {
        assertNotNull(globalExceptionHandler);
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }

    private void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected '" + expected + "' but was '" + actual + "'");
        }
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but was false");
        }
    }
}
