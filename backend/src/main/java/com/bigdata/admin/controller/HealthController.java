package com.bigdata.admin.controller;

import com.bigdata.admin.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "API health status")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check endpoint")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "bigdata-admin");
        health.put("version", "1.0.0");
        health.put("timestamp", System.currentTimeMillis());
        return Result.success(health);
    }
}
