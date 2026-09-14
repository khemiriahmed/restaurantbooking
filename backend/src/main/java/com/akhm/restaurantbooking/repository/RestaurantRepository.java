package com.akhm.restaurantbooking.repository;

import com.akhm.restaurantbooking.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByActiveTrue();

    List<Restaurant> findByCityIgnoreCase(String city);

    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);
}