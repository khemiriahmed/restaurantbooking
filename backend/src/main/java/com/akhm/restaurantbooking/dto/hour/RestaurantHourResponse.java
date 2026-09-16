package com.akhm.restaurantbooking.dto.hour;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record RestaurantHourResponse(

        Long id,
        DayOfWeek dayOfWeek,
        LocalTime openingTime,
        LocalTime closingTime,
        Boolean closed,
        Long restaurantId

) {
}