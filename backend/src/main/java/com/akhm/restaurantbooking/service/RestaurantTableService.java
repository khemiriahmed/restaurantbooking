package com.akhm.restaurantbooking.service;

import com.akhm.restaurantbooking.entity.Restaurant;
import com.akhm.restaurantbooking.entity.RestaurantTable;
import com.akhm.restaurantbooking.enums.RestaurantTableStatus;
import com.akhm.restaurantbooking.exception.BadRequestException;
import com.akhm.restaurantbooking.exception.ResourceNotFoundException;
import com.akhm.restaurantbooking.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final RestaurantService restaurantService;

    public List<RestaurantTable> findAll() {
        return tableRepository.findAll();
    }

    public RestaurantTable findById(Long id) {

        return tableRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Table not found with id: " + id
                        )
                );
    }

    public List<RestaurantTable> findByRestaurant(
            Long restaurantId
    ) {

        restaurantService.findById(restaurantId);

        return tableRepository.findByRestaurantId(
                restaurantId
        );
    }

    public List<RestaurantTable> findAvailableTables(
            Long restaurantId
    ) {

        restaurantService.findById(restaurantId);

        return tableRepository
                .findByRestaurantIdAndStatus(
                        restaurantId,
                        RestaurantTableStatus.AVAILABLE
                );
    }

    public List<RestaurantTable> findTablesWithCapacity(
            Long restaurantId,
            Integer numberOfPeople
    ) {

        if (numberOfPeople == null
                || numberOfPeople <= 0) {

            throw new BadRequestException(
                    "Number of people must be greater than 0"
            );
        }

        restaurantService.findById(restaurantId);

        return tableRepository
                .findByRestaurantIdAndCapacityGreaterThanEqual(
                        restaurantId,
                        numberOfPeople
                );
    }

    public RestaurantTable save(
            Long restaurantId,
            RestaurantTable table
    ) {

        Restaurant restaurant =
                restaurantService.findById(restaurantId);

        if (table.getCapacity() == null
                || table.getCapacity() <= 0) {

            throw new BadRequestException(
                    "Table capacity must be greater than 0"
            );
        }

        table.setRestaurant(restaurant);

        return tableRepository.save(table);
    }

    public RestaurantTable update(
            Long id,
            RestaurantTable updatedTable
    ) {

        RestaurantTable existingTable = findById(id);

        if (updatedTable.getCapacity() == null
                || updatedTable.getCapacity() <= 0) {

            throw new BadRequestException(
                    "Table capacity must be greater than 0"
            );
        }

        existingTable.setTableNumber(
                updatedTable.getTableNumber()
        );

        existingTable.setCapacity(
                updatedTable.getCapacity()
        );

        existingTable.setStatus(
                updatedTable.getStatus()
        );

        return tableRepository.save(existingTable);
    }

    public void changeStatus(
            Long id,
            RestaurantTableStatus status
    ) {

        RestaurantTable table = findById(id);

        if (status == null) {

            throw new BadRequestException(
                    "Table status is required"
            );
        }

        table.setStatus(status);

        tableRepository.save(table);
    }

    public void delete(Long id) {

        RestaurantTable table = findById(id);

        tableRepository.delete(table);
    }
}