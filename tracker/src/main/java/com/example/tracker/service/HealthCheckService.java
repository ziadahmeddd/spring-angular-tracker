package com.example.tracker.service;

import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

    public String getStatus() {
        return "Core container running. IoC container initialized successfully.";
    }
}