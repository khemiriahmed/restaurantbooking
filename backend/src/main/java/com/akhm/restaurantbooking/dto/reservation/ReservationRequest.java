package com.akhm.restaurantbooking.dto.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequest(

        @NotNull
        Long userId,

        @NotNull
        Long restaurantId,

        @NotNull
        Long tableId,

        @NotNull
        @FutureOrPresent
        LocalDate reservationDate,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        @Min(value = 1)
        Integer numberOfPeople

) {
}