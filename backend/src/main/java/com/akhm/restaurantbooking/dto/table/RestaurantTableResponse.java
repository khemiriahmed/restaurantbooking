package com.akhm.restaurantbooking.dto.table;

import com.akhm.restaurantbooking.enums.RestaurantTableStatus;

public record RestaurantTableResponse(

        Long id,
        Integer tableNumber,
        Integer capacity,
        RestaurantTableStatus status,
        Long restaurantId

) {
}