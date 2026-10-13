package com.akhm.restaurantbooking.service;

import com.akhm.restaurantbooking.entity.Reservation;
import com.akhm.restaurantbooking.entity.Restaurant;
import com.akhm.restaurantbooking.entity.RestaurantTable;
import com.akhm.restaurantbooking.entity.User;
import com.akhm.restaurantbooking.enums.ReservationStatus;
import com.akhm.restaurantbooking.enums.RestaurantTableStatus;
import com.akhm.restaurantbooking.enums.RoleName;
import com.akhm.restaurantbooking.exception.BadRequestException;
import com.akhm.restaurantbooking.exception.ConflictException;
import com.akhm.restaurantbooking.exception.ResourceNotFoundException;
import com.akhm.restaurantbooking.repository.ReservationRepository;
import com.akhm.restaurantbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Liste les réservations accessibles à l'utilisateur connecté :
     * toutes pour un ADMIN, celles de ses restaurants pour un
     * RESTAURANT, les siennes pour un CLIENT.
     */
    public List<Reservation> findAll(String authenticatedEmail) {

        User user = getAuthenticatedUser(authenticatedEmail);

        return switch (user.getRole().getName()) {
            case ADMIN -> reservationRepository.findAll();
            case RESTAURANT -> reservationRepository
                    .findByRestaurantOwnerId(user.getId());
            case CLIENT -> reservationRepository
                    .findByUserId(user.getId());
        };
    }

    public Reservation findById(
            Long id,
            String authenticatedEmail
    ) {

        Reservation reservation = getReservation(id);

        checkCanView(reservation, getAuthenticatedUser(authenticatedEmail));

        return reservation;
    }

    public List<Reservation> findByUser(
            Long userId,
            String authenticatedEmail
    ) {

        User user = getAuthenticatedUser(authenticatedEmail);

        if (!isAdmin(user) && !user.getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You are not allowed to access these reservations"
            );
        }

        userService.findById(userId);

        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> findByRestaurant(
            Long restaurantId,
            String authenticatedEmail
    ) {

        Restaurant restaurant = restaurantService.findById(restaurantId);

        checkRestaurantManager(restaurant, getAuthenticatedUser(authenticatedEmail));

        return reservationRepository.findByRestaurantId(restaurantId);
    }

    public List<Reservation> findByTable(
            Long tableId,
            String authenticatedEmail
    ) {

        RestaurantTable table = tableService.findById(tableId);

        checkRestaurantManager(
                table.getRestaurant(),
                getAuthenticatedUser(authenticatedEmail)
        );

        return reservationRepository.findByRestaurantTableId(tableId);
    }

    @Transactional
    public Reservation create(
            Long restaurantId,
            Long tableId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer numberOfPeople,
            String authenticatedEmail
    ) {

        // 1. Validation des données
        validateReservationData(
                date,
                startTime,
                endTime,
                numberOfPeople
        );

        // 2. Vérifier utilisateur
        User user = getAuthenticatedUser(authenticatedEmail);

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

        if (!hourService.isRestaurantOpen(restaurantId, day, startTime)) {

            throw new ConflictException(
                    "Restaurant is closed at this time"
            );
        }

        if (!hourService.isRestaurantOpen(restaurantId, day, endTime)) {

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

        return reservationRepository.save(reservation);
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

    /**
     * Confirmation par le restaurateur (ou un administrateur).
     */
    public Reservation confirm(Long id, String authenticatedEmail) {

        Reservation reservation = getReservation(id);

        checkRestaurantManager(
                reservation.getRestaurant(),
                getAuthenticatedUser(authenticatedEmail)
        );

        if (reservation.getStatus() != ReservationStatus.PENDING) {

            throw new ConflictException(
                    "Only pending reservations can be confirmed"
            );
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservationRepository.save(reservation);
    }

    public Reservation reject(Long id, String authenticatedEmail) {

        Reservation reservation = getReservation(id);

        checkRestaurantManager(
                reservation.getRestaurant(),
                getAuthenticatedUser(authenticatedEmail)
        );

        if (reservation.getStatus() != ReservationStatus.PENDING) {

            throw new ConflictException(
                    "Only pending reservations can be rejected"
            );
        }

        reservation.setStatus(ReservationStatus.REJECTED);

        return reservationRepository.save(reservation);
    }

    /**
     * Annulation possible par le client propriétaire,
     * le restaurateur ou un administrateur.
     */
    public Reservation cancel(Long id, String authenticatedEmail) {

        Reservation reservation = getReservation(id);

        User user = getAuthenticatedUser(authenticatedEmail);

        if (!isAdmin(user)
                && !isReservationOwner(reservation, user)
                && !isRestaurantOwner(reservation, user)) {

            throw new AccessDeniedException(
                    "You are not allowed to cancel this reservation"
            );
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {

            throw new ConflictException(
                    "Reservation is already cancelled"
            );
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {

            throw new ConflictException(
                    "Completed reservation cannot be cancelled"
            );
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        return reservationRepository.save(reservation);
    }

    public Reservation complete(Long id, String authenticatedEmail) {

        Reservation reservation = getReservation(id);

        checkRestaurantManager(
                reservation.getRestaurant(),
                getAuthenticatedUser(authenticatedEmail)
        );

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {

            throw new ConflictException(
                    "Only confirmed reservations can be completed"
            );
        }

        reservation.setStatus(ReservationStatus.COMPLETED);

        return reservationRepository.save(reservation);
    }

    public void delete(Long id, String authenticatedEmail) {

        Reservation reservation = getReservation(id);

        User user = getAuthenticatedUser(authenticatedEmail);

        if (!isAdmin(user) && !isReservationOwner(reservation, user)) {

            throw new AccessDeniedException(
                    "You are not allowed to delete this reservation"
            );
        }

        reservationRepository.delete(reservation);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Reservation getReservation(Long id) {

        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id
                        )
                );
    }

    private User getAuthenticatedUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );
    }

    private boolean isAdmin(User user) {
        return user.getRole().getName() == RoleName.ADMIN;
    }

    private boolean isReservationOwner(Reservation reservation, User user) {
        return reservation.getUser().getId().equals(user.getId());
    }

    private boolean isRestaurantOwner(Reservation reservation, User user) {

        User owner = reservation.getRestaurant().getOwner();

        return owner != null && owner.getId().equals(user.getId());
    }

    private void checkCanView(Reservation reservation, User user) {

        if (isAdmin(user)
                || isReservationOwner(reservation, user)
                || isRestaurantOwner(reservation, user)) {

            return;
        }

        throw new AccessDeniedException(
                "You are not allowed to access this reservation"
        );
    }

    private void checkRestaurantManager(Restaurant restaurant, User user) {

        if (isAdmin(user)) {
            return;
        }

        User owner = restaurant.getOwner();

        if (owner == null || !owner.getId().equals(user.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to manage this restaurant"
            );
        }
    }
}
