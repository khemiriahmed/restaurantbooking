package com.akhm.restaurantbooking.service;

import com.akhm.restaurantbooking.entity.Reservation;
import com.akhm.restaurantbooking.entity.Restaurant;
import com.akhm.restaurantbooking.entity.RestaurantTable;
import com.akhm.restaurantbooking.entity.User;
import com.akhm.restaurantbooking.enums.ReservationStatus;
import com.akhm.restaurantbooking.enums.RestaurantTableStatus;
import com.akhm.restaurantbooking.exception.BadRequestException;
import com.akhm.restaurantbooking.exception.ConflictException;
import com.akhm.restaurantbooking.exception.ResourceNotFoundException;
import com.akhm.restaurantbooking.repository.ReservationRepository;
import com.akhm.restaurantbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final RestaurantTableService tableService;
    private final RestaurantHourService hourService;

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findById(
            Long id,
            String authenticatedEmail
    ) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id
                        )
                );

        User authenticatedUser =
                getAuthenticatedUser(authenticatedEmail);

        if (!reservation.getUser().getId()
                .equals(authenticatedUser.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this reservation"
            );
        }

        return reservation;
    }

    private User getAuthenticatedUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );
    }

    public List<Reservation> findByUser(Long userId) {

        userService.findById(userId);

        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> findByRestaurant(
            Long restaurantId
    ) {

        restaurantService.findById(restaurantId);

        return reservationRepository
                .findByRestaurantId(restaurantId);
    }

    public List<Reservation> findByTable(
            Long tableId
    ) {

        tableService.findById(tableId);

        return reservationRepository
                .findByRestaurantTableId(tableId);
    }

    public Reservation create(
            Long userId,
            Long restaurantId,
            Long tableId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer numberOfPeople
    ) {

        // 1. Validation des données
        validateReservationData(
                date,
                startTime,
                endTime,
                numberOfPeople
        );

        // 2. Vérifier utilisateur
        User user = userService.findById(userId);

        if (!Boolean.TRUE.equals(user.getEnabled())) {

            throw new ConflictException(
                    "User account is disabled"
            );
        }

        // 3. Vérifier restaurant
        Restaurant restaurant =
                restaurantService.findById(restaurantId);

        // 4. Restaurant actif
        if (!Boolean.TRUE.equals(restaurant.getActive())) {

            throw new ConflictException(
                    "Restaurant is inactive"
            );
        }

        // 5. Vérifier table
        RestaurantTable table =
                tableService.findById(tableId);

        // 6. Table appartient au restaurant
        if (!table.getRestaurant()
                .getId()
                .equals(restaurantId)) {

            throw new BadRequestException(
                    "The table does not belong to this restaurant"
            );
        }

        // 7. Vérifier statut table
        if (table.getStatus()
                != RestaurantTableStatus.AVAILABLE) {

            throw new ConflictException(
                    "Table is not available"
            );
        }

        // 8. Vérifier capacité
        if (table.getCapacity() < numberOfPeople) {

            throw new ConflictException(
                    "Table capacity is insufficient"
            );
        }

        // 9. Vérifier horaires
        DayOfWeek day = date.getDayOfWeek();

        boolean restaurantOpen =
                hourService.isRestaurantOpen(
                        restaurantId,
                        day,
                        startTime
                );

        if (!restaurantOpen) {

            throw new ConflictException(
                    "Restaurant is closed at this time"
            );
        }

        boolean endInsideOpeningHours =
                hourService.isRestaurantOpen(
                        restaurantId,
                        day,
                        endTime
                );

        if (!endInsideOpeningHours) {

            throw new ConflictException(
                    "Reservation ends after restaurant closing time"
            );
        }

        // 10. Vérifier chevauchement
        boolean alreadyReserved =
                reservationRepository
                        .existsByRestaurantTableIdAndReservationDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                                tableId,
                                date,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.CONFIRMED
                                ),
                                endTime,
                                startTime
                        );

        if (alreadyReserved) {

            throw new ConflictException(
                    "Table is already reserved during this period"
            );
        }

        // 11. Créer réservation
        Reservation reservation =
                Reservation.builder()
                        .reservationDate(date)
                        .startTime(startTime)
                        .endTime(endTime)
                        .numberOfPeople(numberOfPeople)
                        .status(ReservationStatus.PENDING)
                        .user(user)
                        .restaurant(restaurant)
                        .restaurantTable(table)
                        .build();

        return reservationRepository.save(
                reservation
        );
    }

    private void validateReservationData(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer numberOfPeople
    ) {

        if (date == null) {

            throw new BadRequestException(
                    "Reservation date is required"
            );
        }

        if (startTime == null
                || endTime == null) {

            throw new BadRequestException(
                    "Start and end time are required"
            );
        }

        if (!startTime.isBefore(endTime)) {

            throw new BadRequestException(
                    "Start time must be before end time"
            );
        }

        if (numberOfPeople == null
                || numberOfPeople <= 0) {

            throw new BadRequestException(
                    "Number of people must be greater than 0"
            );
        }

        if (date.isBefore(LocalDate.now())) {

            throw new BadRequestException(
                    "Reservation date cannot be in the past"
            );
        }
    }

    public Reservation confirm(Long id, String authenticatedEmail) {

        Reservation reservation = findById(id,authenticatedEmail);

        if (reservation.getStatus()
                != ReservationStatus.PENDING) {

            throw new ConflictException(
                    "Only pending reservations can be confirmed"
            );
        }

        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        return reservationRepository.save(
                reservation
        );
    }

    public Reservation reject(Long id, String authenticatedEmail) {

        Reservation reservation = findById(id, authenticatedEmail);

        if (reservation.getStatus()
                != ReservationStatus.PENDING) {

            throw new ConflictException(
                    "Only pending reservations can be rejected"
            );
        }

        reservation.setStatus(
                ReservationStatus.REJECTED
        );

        return reservationRepository.save(
                reservation
        );
    }

    public Reservation cancel(Long id, String authenticatedEmail) {

        Reservation reservation = findById(id, authenticatedEmail);

        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new ConflictException(
                    "Reservation is already cancelled"
            );
        }

        if (reservation.getStatus()
                == ReservationStatus.COMPLETED) {

            throw new ConflictException(
                    "Completed reservation cannot be cancelled"
            );
        }

        reservation.setStatus(
                ReservationStatus.CANCELLED
        );

        return reservationRepository.save(
                reservation
        );
    }

    public Reservation complete(Long id, String authenticatedEmail) {

        Reservation reservation = findById(id, authenticatedEmail);

        if (reservation.getStatus()
                != ReservationStatus.CONFIRMED) {

            throw new ConflictException(
                    "Only confirmed reservations can be completed"
            );
        }

        reservation.setStatus(
                ReservationStatus.COMPLETED
        );

        return reservationRepository.save(
                reservation
        );
    }

    public void delete(Long id, String authenticatedEmail) {

        Reservation reservation = findById(id, authenticatedEmail);

        reservationRepository.delete(reservation);
    }
}