package com.akhm.restaurantbooking.dto.restaurant;

import java.math.BigDecimal;

public record RestaurantResponse(

        Long id,
        String name,
        String description,
        String address,
        String city,
        String phone,
        String email,
        String cuisineType,
        BigDecimal averagePrice,
        String imageUrl,
        Boolean active

) {
}