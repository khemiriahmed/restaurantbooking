package com.akhm.restaurantbooking.controller;

import com.akhm.restaurantbooking.dto.restaurant.RestaurantRequest;
import com.akhm.restaurantbooking.dto.restaurant.RestaurantResponse;
import com.akhm.restaurantbooking.entity.Restaurant;
import com.akhm.restaurantbooking.entity.User;
import com.akhm.restaurantbooking.service.RestaurantService;
import com.akhm.restaurantbooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final UserService userService;

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

    @GetMapping("/mine")
    public List<RestaurantResponse> findMine(Authentication authentication) {

        User user = userService.findByEmail(authentication.getName());

        return restaurantService
                .findByOwner(user.getId())
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
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication
    ) {

        User owner = userService.findByEmail(authentication.getName());

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
                .owner(owner)
                .build();

        return toResponse(
                restaurantService.save(restaurant)
        );
    }

    @PutMapping("/{id}")
    public RestaurantResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            Authentication authentication
    ) {

        checkManagePermission(id, authentication);

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
    public RestaurantResponse activate(
            @PathVariable Long id,
            Authentication authentication
    ) {

        checkManagePermission(id, authentication);

        return toResponse(restaurantService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public RestaurantResponse deactivate(
            @PathVariable Long id,
            Authentication authentication
    ) {

        checkManagePermission(id, authentication);

        return toResponse(restaurantService.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {

        checkManagePermission(id, authentication);

        restaurantService.delete(id);
    }

    /**
     * Autorise l'action si l'utilisateur est ADMIN
     * ou le propriétaire du restaurant.
     */
    private void checkManagePermission(
            Long restaurantId,
            Authentication authentication
    ) {

        restaurantService.checkManagePermission(
                restaurantId,
                authentication.getName()
        );
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
