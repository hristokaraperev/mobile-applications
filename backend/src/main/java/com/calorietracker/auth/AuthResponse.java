package com.calorietracker.auth;

public record AuthResponse(String accessToken, UserResponse user) {}
