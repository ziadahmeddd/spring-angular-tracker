package com.example.tracker.controller;

import com.example.tracker.service.HealthCheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    // Constructor Injection: Spring sees this constructor, looks up HealthCheckService in the container, and passes it in.
    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/api/health")
    public String checkHealth() {
        return healthCheckService.getStatus();
    }
}