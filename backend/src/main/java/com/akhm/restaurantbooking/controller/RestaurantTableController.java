package com.akhm.restaurantbooking.controller;

import com.akhm.restaurantbooking.dto.table.RestaurantTableRequest;
import com.akhm.restaurantbooking.dto.table.RestaurantTableResponse;
import com.akhm.restaurantbooking.entity.RestaurantTable;
import com.akhm.restaurantbooking.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableService tableService;

    @GetMapping("/api/restaurants/{restaurantId}/tables")
    public List<RestaurantTableResponse> findByRestaurant(
            @PathVariable Long restaurantId
    ) {

        return tableService
                .findByRestaurant(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/api/restaurants/{restaurantId}/tables/available")
    public List<RestaurantTableResponse> findAvailable(
            @PathVariable Long restaurantId
    ) {

        return tableService
                .findAvailableTables(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping(
            "/api/restaurants/{restaurantId}/tables/capacity/{people}"
    )
    public List<RestaurantTableResponse> findByCapacity(
            @PathVariable Long restaurantId,
            @PathVariable Integer people
    ) {

        return tableService
                .findTablesWithCapacity(
                        restaurantId,
                        people
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/api/tables/{id}")
    public RestaurantTableResponse findById(
            @PathVariable Long id
    ) {

        return toResponse(
                tableService.findById(id)
        );
    }

    @PostMapping("/api/restaurants/{restaurantId}/tables")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantTableResponse create(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantTableRequest request
    ) {

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status(request.status())
                .build();

        return toResponse(
                tableService.save(
                        restaurantId,
                        table
                )
        );
    }

    @PutMapping("/api/tables/{id}")
    public RestaurantTableResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantTableRequest request
    ) {

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status(request.status())
                .build();

        return toResponse(
                tableService.update(id, table)
        );
    }

    @PatchMapping("/api/tables/{id}/status")
    public void changeStatus(
            @PathVariable Long id,
            @RequestParam com.akhm.restaurantbooking.enums.RestaurantTableStatus status
    ) {

        tableService.changeStatus(id, status);
    }

    @DeleteMapping("/api/tables/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {

        tableService.delete(id);
    }

    private RestaurantTableResponse toResponse(
            RestaurantTable table
    ) {

        return new RestaurantTableResponse(
                table.getId(),
                table.getTableNumber(),
                table.getCapacity(),
                table.getStatus(),
                table.getRestaurant().getId()
        );
    }
}