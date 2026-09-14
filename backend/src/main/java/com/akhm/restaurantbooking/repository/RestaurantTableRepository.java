package com.akhm.restaurantbooking.repository;

import com.akhm.restaurantbooking.entity.RestaurantTable;
import com.akhm.restaurantbooking.enums.RestaurantTableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantTableRepository
        extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByRestaurantId(Long restaurantId);

    List<RestaurantTable> findByRestaurantIdAndStatus(
            Long restaurantId,
            RestaurantTableStatus status
    );

    List<RestaurantTable> findByRestaurantIdAndCapacityGreaterThanEqual(
            Long restaurantId,
            Integer capacity
    );
}