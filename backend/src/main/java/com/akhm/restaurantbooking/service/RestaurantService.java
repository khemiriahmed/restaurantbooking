package com.akhm.restaurantbooking.service;

import com.akhm.restaurantbooking.entity.Restaurant;
import com.akhm.restaurantbooking.exception.BadRequestException;
import com.akhm.restaurantbooking.exception.ResourceNotFoundException;
import com.akhm.restaurantbooking.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public List<Restaurant> findActiveRestaurants() {
        return restaurantRepository.findByActiveTrue();
    }

    public List<Restaurant> findByCity(String city) {
        return restaurantRepository.findByCityIgnoreCase(city);
    }

    public List<Restaurant> findByCuisine(String cuisineType) {
        return restaurantRepository
                .findByCuisineTypeIgnoreCase(cuisineType);
    }

    public Restaurant findById(Long id) {

        return restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant not found with id: " + id
                        )
                );
    }

    public Restaurant save(Restaurant restaurant) {

        if (restaurant.getName() == null
                || restaurant.getName().isBlank()) {

            throw new BadRequestException(
                    "Restaurant name is required"
            );
        }

        return restaurantRepository.save(restaurant);
    }

    public Restaurant update(
            Long id,
            Restaurant updatedRestaurant
    ) {

        Restaurant existingRestaurant = findById(id);

        existingRestaurant.setName(
                updatedRestaurant.getName()
        );

        existingRestaurant.setDescription(
                updatedRestaurant.getDescription()
        );

        existingRestaurant.setAddress(
                updatedRestaurant.getAddress()
        );

        existingRestaurant.setCity(
                updatedRestaurant.getCity()
        );

        existingRestaurant.setPhone(
                updatedRestaurant.getPhone()
        );

        existingRestaurant.setEmail(
                updatedRestaurant.getEmail()
        );

        existingRestaurant.setCuisineType(
                updatedRestaurant.getCuisineType()
        );

        existingRestaurant.setAveragePrice(
                updatedRestaurant.getAveragePrice()
        );

        existingRestaurant.setImageUrl(
                updatedRestaurant.getImageUrl()
        );

        return restaurantRepository.save(existingRestaurant);
    }

    public void activate(Long id) {

        Restaurant restaurant = findById(id);

        restaurant.setActive(true);

        restaurantRepository.save(restaurant);
    }

    public void deactivate(Long id) {

        Restaurant restaurant = findById(id);

        restaurant.setActive(false);

        restaurantRepository.save(restaurant);
    }

    public void delete(Long id) {

        Restaurant restaurant = findById(id);

        restaurantRepository.delete(restaurant);
    }
}