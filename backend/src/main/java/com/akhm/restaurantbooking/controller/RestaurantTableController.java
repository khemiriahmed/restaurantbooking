package com.akhm.restaurantbooking.controller;

import com.akhm.restaurantbooking.dto.table.RestaurantTableRequest;
import com.akhm.restaurantbooking.dto.table.RestaurantTableResponse;
import com.akhm.restaurantbooking.entity.RestaurantTable;
import com.akhm.restaurantbooking.enums.RestaurantTableStatus;
import com.akhm.restaurantbooking.service.RestaurantService;
import com.akhm.restaurantbooking.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableService tableService;
    private final RestaurantService restaurantService;

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
                .findTablesWithCapacity(restaurantId, people)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/api/tables/{id}")
    public RestaurantTableResponse findById(
            @PathVariable Long id
    ) {

        return toResponse(tableService.findById(id));
    }

    @PostMapping("/api/restaurants/{restaurantId}/tables")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantTableResponse create(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantTableRequest request,
            Authentication authentication
    ) {

        restaurantService.checkManagePermission(
                restaurantId,
                authentication.getName()
        );

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status(request.status())
                .build();

        return toResponse(tableService.save(restaurantId, table));
    }

    @PutMapping("/api/tables/{id}")
    public RestaurantTableResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantTableRequest request,
            Authentication authentication
    ) {

        RestaurantTable existing = tableService.findById(id);

        restaurantService.checkManagePermission(
                existing.getRestaurant().getId(),
                authentication.getName()
        );

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status(request.status())
                .build();

        return toResponse(tableService.update(id, table));
    }

    @PatchMapping("/api/tables/{id}/status")
    public RestaurantTableResponse changeStatus(
            @PathVariable Long id,
            @RequestParam RestaurantTableStatus status,
            Authentication authentication
    ) {

        RestaurantTable existing = tableService.findById(id);

        restaurantService.checkManagePermission(
                existing.getRestaurant().getId(),
                authentication.getName()
        );

        tableService.changeStatus(id, status);

        return toResponse(tableService.findById(id));
    }

    @DeleteMapping("/api/tables/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {

        RestaurantTable existing = tableService.findById(id);

        restaurantService.checkManagePermission(
                existing.getRestaurant().getId(),
                authentication.getName()
        );

        tableService.delete(id);
    }

    private RestaurantTableResponse toResponse(RestaurantTable table) {

        return new RestaurantTableResponse(
                table.getId(),
                table.getTableNumber(),
                table.getCapacity(),
                table.getStatus(),
                table.getRestaurant().getId()
        );
    }
}
