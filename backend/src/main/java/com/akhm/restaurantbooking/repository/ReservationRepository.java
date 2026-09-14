package com.akhm.restaurantbooking.repository;

import com.akhm.restaurantbooking.entity.Reservation;
import com.akhm.restaurantbooking.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByRestaurantId(Long restaurantId);

    List<Reservation> findByRestaurantTableId(Long tableId);

    List<Reservation> findByReservationDate(LocalDate date);

    List<Reservation> findByStatus(ReservationStatus status);
}