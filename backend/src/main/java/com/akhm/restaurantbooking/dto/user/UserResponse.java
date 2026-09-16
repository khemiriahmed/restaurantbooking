package com.akhm.restaurantbooking.dto.user;

import com.akhm.restaurantbooking.enums.RoleName;

import java.time.LocalDateTime;

public record UserResponse(

        Long id,

        String firstName,

        String lastName,

        String email,

        String phone,

        Boolean enabled,

        RoleName role,

        LocalDateTime createdAt
) {
}