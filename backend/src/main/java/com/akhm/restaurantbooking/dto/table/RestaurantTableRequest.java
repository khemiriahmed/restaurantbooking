package com.akhm.restaurantbooking.dto.table;

import com.akhm.restaurantbooking.enums.RestaurantTableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RestaurantTableRequest(

        @NotNull(message = "Table number is required")
        @Min(value = 1, message = "Table number must be positive")
        Integer tableNumber,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be greater than 0")
        Integer capacity,

        RestaurantTableStatus status

) {
}