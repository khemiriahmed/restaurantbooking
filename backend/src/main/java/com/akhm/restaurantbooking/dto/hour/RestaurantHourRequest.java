package com.akhm.restaurantbooking.dto.hour;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record RestaurantHourRequest(

        @NotNull
        DayOfWeek dayOfWeek,

        LocalTime openingTime,

        LocalTime closingTime,

        Boolean closed

) {
}