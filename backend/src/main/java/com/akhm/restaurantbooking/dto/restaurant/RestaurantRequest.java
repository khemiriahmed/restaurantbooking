package com.akhm.restaurantbooking.dto.restaurant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RestaurantRequest(

        @NotBlank(message = "Restaurant name is required")
        @Size(max = 150)
        String name,

        String description,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        @Size(max = 30)
        String phone,

        @Email(message = "Invalid email")
        String email,

        String cuisineType,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal averagePrice,

        String imageUrl
) {
}