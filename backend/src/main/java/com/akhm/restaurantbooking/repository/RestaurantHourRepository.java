package com.akhm.restaurantbooking.repository;

import com.akhm.restaurantbooking.entity.RestaurantHour;
import com.akhm.restaurantbooking.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface RestaurantHourRepository
        extends JpaRepository<RestaurantHour, Long> {

    Optional<RestaurantHour> findByRestaurantIdAndDayOfWeek(
            Long restaurantId,
            DayOfWeek dayOfWeek
    );
    List<RestaurantHour> findByRestaurantId(Long restaurantId);
}