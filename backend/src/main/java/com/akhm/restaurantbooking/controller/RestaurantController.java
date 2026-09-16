package com.akhm.restaurantbooking.controller;

import com.akhm.restaurantbooking.dto.restaurant.RestaurantRequest;
import com.akhm.restaurantbooking.dto.restaurant.RestaurantResponse;
import com.akhm.restaurantbooking.entity.Restaurant;
import com.akhm.restaurantbooking.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public List<RestaurantResponse> findAll() {

        return restaurantService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/active")
    public List<RestaurantResponse> findActive() {

        return restaurantService
                .findActiveRestaurants()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public RestaurantResponse findById(
            @PathVariable Long id
    ) {

        return toResponse(
                restaurantService.findById(id)
        );
    }

    @GetMapping("/city/{city}")
    public List<RestaurantResponse> findByCity(
            @PathVariable String city
    ) {

        return restaurantService
                .findByCity(city)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/cuisine/{cuisineType}")
    public List<RestaurantResponse> findByCuisine(
            @PathVariable String cuisineType
    ) {

        return restaurantService
                .findByCuisine(cuisineType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse create(
            @Valid @RequestBody RestaurantRequest request
    ) {

        Restaurant restaurant = Restaurant.builder()
                .name(request.name())
                .description(request.description())
                .address(request.address())
                .city(request.city())
                .phone(request.phone())
                .email(request.email())
                .cuisineType(request.cuisineType())
                .averagePrice(request.averagePrice())
                .imageUrl(request.imageUrl())
                .build();

        return toResponse(
                restaurantService.save(restaurant)
        );
    }

    @PutMapping("/{id}")
    public RestaurantResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request
    ) {

        Restaurant restaurant = Restaurant.builder()
                .name(request.name())
                .description(request.description())
                .address(request.address())
                .city(request.city())
                .phone(request.phone())
                .email(request.email())
                .cuisineType(request.cuisineType())
                .averagePrice(request.averagePrice())
                .imageUrl(request.imageUrl())
                .build();

        return toResponse(
                restaurantService.update(id, restaurant)
        );
    }

    @PatchMapping("/{id}/activate")
    public void activate(
            @PathVariable Long id
    ) {

        restaurantService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(
            @PathVariable Long id
    ) {

        restaurantService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {

        restaurantService.delete(id);
    }

    private RestaurantResponse toResponse(
            Restaurant restaurant
    ) {

        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getAddress(),
                restaurant.getCity(),
                restaurant.getPhone(),
                restaurant.getEmail(),
                restaurant.getCuisineType(),
                restaurant.getAveragePrice(),
                restaurant.getImageUrl(),
                restaurant.getActive()
        );
    }
}