package com.akhm.restaurantbooking.controller;

import com.akhm.restaurantbooking.dto.hour.RestaurantHourRequest;
import com.akhm.restaurantbooking.dto.hour.RestaurantHourResponse;
import com.akhm.restaurantbooking.entity.RestaurantHour;
import com.akhm.restaurantbooking.service.RestaurantHourService;
import com.akhm.restaurantbooking.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestaurantHourController {

    private final RestaurantHourService hourService;
    private final RestaurantService restaurantService;

    @GetMapping("/api/restaurants/{restaurantId}/hours")
    public List<RestaurantHourResponse> findAll(
            @PathVariable Long restaurantId
    ) {

        return hourService.findByRestaurant(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/api/restaurants/{restaurantId}/hours/{day}")
    public RestaurantHourResponse findByDay(
            @PathVariable Long restaurantId,
            @PathVariable DayOfWeek day
    ) {

        return toResponse(
                hourService.findByRestaurantAndDay(restaurantId, day)
        );
    }

    @GetMapping("/api/hours/{id}")
    public RestaurantHourResponse findById(
            @PathVariable Long id
    ) {

        return toResponse(hourService.findById(id));
    }

    @PostMapping("/api/restaurants/{restaurantId}/hours")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantHourResponse create(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantHourRequest request,
            Authentication authentication
    ) {

        restaurantService.checkManagePermission(
                restaurantId,
                authentication.getName()
        );

        RestaurantHour hour = RestaurantHour.builder()
                .dayOfWeek(request.dayOfWeek())
                .openingTime(request.openingTime())
                .closingTime(request.closingTime())
                .closed(request.closed())
                .build();

        return toResponse(hourService.save(restaurantId, hour));
    }

    @PutMapping("/api/hours/{id}")
    public RestaurantHourResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantHourRequest request,
            Authentication authentication
    ) {

        RestaurantHour existing = hourService.findById(id);

        restaurantService.checkManagePermission(
                existing.getRestaurant().getId(),
                authentication.getName()
        );

        RestaurantHour hour = RestaurantHour.builder()
                .dayOfWeek(request.dayOfWeek())
                .openingTime(request.openingTime())
                .closingTime(request.closingTime())
                .closed(request.closed())
                .build();

        return toResponse(hourService.update(id, hour));
    }

    @DeleteMapping("/api/hours/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {

        RestaurantHour existing = hourService.findById(id);

        restaurantService.checkManagePermission(
                existing.getRestaurant().getId(),
                authentication.getName()
        );

        hourService.delete(id);
    }

    private RestaurantHourResponse toResponse(RestaurantHour hour) {

        return new RestaurantHourResponse(
                hour.getId(),
                hour.getDayOfWeek(),
                hour.getOpeningTime(),
                hour.getClosingTime(),
                hour.getClosed(),
                hour.getRestaurant().getId()
        );
    }
}
