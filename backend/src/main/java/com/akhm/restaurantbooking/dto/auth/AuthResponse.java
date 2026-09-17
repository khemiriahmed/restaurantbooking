package com.akhm.restaurantbooking.dto.auth;

public record AuthResponse(
        String token,
        String type,
        Long userId,
        String email,
        String role
) {
}