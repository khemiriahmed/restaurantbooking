package com.akhm.restaurantbooking.controller;

import com.akhm.restaurantbooking.dto.reservation.ReservationRequest;
import com.akhm.restaurantbooking.dto.reservation.ReservationResponse;
import com.akhm.restaurantbooking.entity.Reservation;
import com.akhm.restaurantbooking.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public List<ReservationResponse> findAll() {

        return reservationService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ReservationResponse findById(
            @PathVariable Long id
    ) {

        return toResponse(
                reservationService.findById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public List<ReservationResponse> findByUser(
            @PathVariable Long userId
    ) {

        return reservationService
                .findByUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<ReservationResponse> findByRestaurant(
            @PathVariable Long restaurantId
    ) {

        return reservationService
                .findByRestaurant(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/table/{tableId}")
    public List<ReservationResponse> findByTable(
            @PathVariable Long tableId
    ) {

        return reservationService
                .findByTable(tableId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @Valid @RequestBody ReservationRequest request
    ) {

        Reservation reservation =
                reservationService.create(
                        request.userId(),
                        request.restaurantId(),
                        request.tableId(),
                        request.reservationDate(),
                        request.startTime(),
                        request.endTime(),
                        request.numberOfPeople()
                );

        return toResponse(reservation);
    }

    @PatchMapping("/{id}/confirm")
    public ReservationResponse confirm(
            @PathVariable Long id
    ) {

        return toResponse(
                reservationService.confirm(id)
        );
    }

    @PatchMapping("/{id}/reject")
    public ReservationResponse reject(
            @PathVariable Long id
    ) {

        return toResponse(
                reservationService.reject(id)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancel(
            @PathVariable Long id
    ) {

        return toResponse(
                reservationService.cancel(id)
        );
    }

    @PatchMapping("/{id}/complete")
    public ReservationResponse complete(
            @PathVariable Long id
    ) {

        return toResponse(
                reservationService.complete(id)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {

        reservationService.delete(id);
    }

    private ReservationResponse toResponse(
            Reservation reservation
    ) {

        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getNumberOfPeople(),
                reservation.getStatus(),
                reservation.getUser().getId(),
                reservation.getRestaurant().getId(),
                reservation.getRestaurantTable().getId()
        );
    }
}