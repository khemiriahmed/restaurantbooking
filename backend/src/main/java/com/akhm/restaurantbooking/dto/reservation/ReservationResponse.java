package com.akhm.restaurantbooking.dto.reservation;

import com.akhm.restaurantbooking.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(

        Long id,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer numberOfPeople,
        ReservationStatus status,
        Long userId,
        Long restaurantId,
        Long tableId

) {
}