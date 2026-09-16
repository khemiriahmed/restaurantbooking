package com.akhm.restaurantbooking.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        String email,

        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password,

        @Size(max = 30)
        String phone
) {
}