package com.example.tracker.dto;

public record AuthResponse(
    String token,
    String email,
    long expiresIn
) {}
