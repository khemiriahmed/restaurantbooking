package com.akhm.restaurantbooking.dto.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Requête de création d'une réservation.
 * L'utilisateur propriétaire est déduit du token JWT (jamais du client).
 */
public record ReservationRequest(

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
