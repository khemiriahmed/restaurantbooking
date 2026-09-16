package com.akhm.restaurantbooking.service;

import com.akhm.restaurantbooking.entity.Restaurant;
import com.akhm.restaurantbooking.entity.RestaurantHour;
import com.akhm.restaurantbooking.exception.BadRequestException;
import com.akhm.restaurantbooking.exception.ResourceNotFoundException;
import com.akhm.restaurantbooking.repository.RestaurantHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantHourService {

    private final RestaurantHourRepository hourRepository;
    private final RestaurantService restaurantService;

    public List<RestaurantHour> findByRestaurant(Long restaurantId) {

        restaurantService.findById(restaurantId);

        return hourRepository.findByRestaurantId(restaurantId);
    }

    public RestaurantHour findById(Long id) {

        return hourRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant hour not found with id: " + id
                        )
                );
    }

    public RestaurantHour findByRestaurantAndDay(
            Long restaurantId,
            DayOfWeek dayOfWeek
    ) {

        return hourRepository
                .findByRestaurantIdAndDayOfWeek(
                        restaurantId,
                        dayOfWeek
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Opening hours not found for "
                                        + dayOfWeek
                        )
                );
    }

    public RestaurantHour save(
            Long restaurantId,
            RestaurantHour hour
    ) {

        Restaurant restaurant =
                restaurantService.findById(restaurantId);

        validateHour(hour);

        hour.setRestaurant(restaurant);

        return hourRepository.save(hour);
    }

    public RestaurantHour update(
            Long id,
            RestaurantHour updatedHour
    ) {

        RestaurantHour existingHour = findById(id);

        validateHour(updatedHour);

        existingHour.setDayOfWeek(
                updatedHour.getDayOfWeek()
        );

        existingHour.setOpeningTime(
                updatedHour.getOpeningTime()
        );

        existingHour.setClosingTime(
                updatedHour.getClosingTime()
        );

        existingHour.setClosed(
                updatedHour.getClosed()
        );

        return hourRepository.save(existingHour);
    }

    private void validateHour(RestaurantHour hour) {

        if (hour.getDayOfWeek() == null) {

            throw new BadRequestException(
                    "Day of week is required"
            );
        }

        if (hour.getClosed() == null) {
            hour.setClosed(false);
        }

        if (!hour.getClosed()) {

            if (hour.getOpeningTime() == null
                    || hour.getClosingTime() == null) {

                throw new BadRequestException(
                        "Opening and closing time are required"
                );
            }

            if (!hour.getOpeningTime()
                    .isBefore(hour.getClosingTime())) {

                throw new BadRequestException(
                        "Opening time must be before closing time"
                );
            }
        }
    }

    public boolean isRestaurantOpen(
            Long restaurantId,
            DayOfWeek day,
            LocalTime time
    ) {

        RestaurantHour hour =
                findByRestaurantAndDay(
                        restaurantId,
                        day
                );

        if (Boolean.TRUE.equals(hour.getClosed())) {
            return false;
        }

        return !time.isBefore(hour.getOpeningTime())
                && !time.isAfter(hour.getClosingTime());
    }

    public void delete(Long id) {

        RestaurantHour hour = findById(id);

        hourRepository.delete(hour);
    }
}