package com.akhm.restaurantbooking.config;

import com.akhm.restaurantbooking.entity.Restaurant;
import com.akhm.restaurantbooking.entity.RestaurantHour;
import com.akhm.restaurantbooking.entity.RestaurantTable;
import com.akhm.restaurantbooking.entity.Role;
import com.akhm.restaurantbooking.entity.User;
import com.akhm.restaurantbooking.enums.RestaurantTableStatus;
import com.akhm.restaurantbooking.enums.RoleName;
import com.akhm.restaurantbooking.repository.RestaurantHourRepository;
import com.akhm.restaurantbooking.repository.RestaurantRepository;
import com.akhm.restaurantbooking.repository.RestaurantTableRepository;
import com.akhm.restaurantbooking.repository.RoleRepository;
import com.akhm.restaurantbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Initialise les données de référence (rôles) et un jeu de données de
 * démonstration pour pouvoir lancer l'application immédiatement.
 *
 * <p>Comptes de démonstration :</p>
 * <ul>
 *   <li>admin@restaurant-booking.com / Admin@12345 (ADMIN)</li>
 *   <li>owner@restaurant-booking.com / Owner@12345 (RESTAURANT)</li>
 *   <li>client@restaurant-booking.com / Client@12345 (CLIENT)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final LocalTime OPENING_TIME = LocalTime.of(11, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(23, 0);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantHourRepository hourRepository;
    private final RestaurantTableRepository tableRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        createRoleIfNotExists(RoleName.CLIENT);
        createRoleIfNotExists(RoleName.RESTAURANT);
        createRoleIfNotExists(RoleName.ADMIN);

        User admin = createUserIfNotExists(
                "Admin", "RestaurantBooking",
                "admin@restaurant-booking.com", "Admin@12345",
                "+216 00 000 001", RoleName.ADMIN
        );

        User owner = createUserIfNotExists(
                "Karim", "Ben Salah",
                "owner@restaurant-booking.com", "Owner@12345",
                "+216 00 000 002", RoleName.RESTAURANT
        );

        createUserIfNotExists(
                "Ahmed", "Khemiri",
                "client@restaurant-booking.com", "Client@12345",
                "+216 00 000 003", RoleName.CLIENT
        );

        seedDemoRestaurants(admin, owner);

        System.out.println("======================================");
        System.out.println("     DATA INITIALIZATION COMPLETE");
        System.out.println("======================================");
    }

    private void createRoleIfNotExists(RoleName roleName) {

        if (roleRepository.findByName(roleName).isEmpty()) {

            Role role = Role.builder()
                    .name(roleName)
                    .build();

            roleRepository.save(role);

            System.out.println("Role created: " + roleName);
        }
    }

    private User createUserIfNotExists(
            String firstName,
            String lastName,
            String email,
            String rawPassword,
            String phone,
            RoleName roleName
    ) {

        return userRepository.findByEmail(email)
                .orElseGet(() -> {

                    Role role = roleRepository.findByName(roleName)
                            .orElseThrow();

                    User user = User.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .password(passwordEncoder.encode(rawPassword))
                            .phone(phone)
                            .enabled(true)
                            .role(role)
                            .build();

                    System.out.println("User created: " + email);

                    return userRepository.save(user);
                });
    }

    private void seedDemoRestaurants(User admin, User owner) {

        if (restaurantRepository.count() > 0) {
            return;
        }

        Restaurant leGourmet = restaurantRepository.save(
                Restaurant.builder()
                        .name("Le Gourmet")
                        .description("Cuisine française raffinée dans un cadre élégant.")
                        .address("12 Avenue Habib Bourguiba")
                        .city("Tunis")
                        .phone("+216 71 000 100")
                        .email("contact@legourmet.tn")
                        .cuisineType("Française")
                        .averagePrice(new BigDecimal("65.00"))
                        .imageUrl("https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800")
                        .active(true)
                        .owner(owner)
                        .build()
        );

        Restaurant laMarina = restaurantRepository.save(
                Restaurant.builder()
                        .name("La Marina")
                        .description("Poissons et fruits de mer face à la mer.")
                        .address("Port de plaisance")
                        .city("Sousse")
                        .phone("+216 73 000 200")
                        .email("contact@lamarina.tn")
                        .cuisineType("Fruits de mer")
                        .averagePrice(new BigDecimal("55.00"))
                        .imageUrl("https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800")
                        .active(true)
                        .owner(owner)
                        .build()
        );

        Restaurant atlas = restaurantRepository.save(
                Restaurant.builder()
                        .name("Atlas Grill")
                        .description("Grillades et spécialités tunisiennes.")
                        .address("Rue de la Liberté")
                        .city("Tunis")
                        .phone("+216 71 000 300")
                        .email("contact@atlasgrill.tn")
                        .cuisineType("Tunisienne")
                        .averagePrice(new BigDecimal("35.00"))
                        .imageUrl("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800")
                        .active(true)
                        .owner(admin)
                        .build()
        );

        seedHoursAndTables(leGourmet, 4);
        seedHoursAndTables(laMarina, 3);
        seedHoursAndTables(atlas, 5);

        System.out.println("Demo restaurants created");
    }

    private void seedHoursAndTables(Restaurant restaurant, int tableCount) {

        for (DayOfWeek day : DayOfWeek.values()) {

            boolean closed = day == DayOfWeek.MONDAY;

            hourRepository.save(
                    RestaurantHour.builder()
                            .dayOfWeek(day)
                            .openingTime(closed ? null : OPENING_TIME)
                            .closingTime(closed ? null : CLOSING_TIME)
                            .closed(closed)
                            .restaurant(restaurant)
                            .build()
            );
        }

        List<Integer> capacities = List.of(2, 2, 4, 4, 6, 8, 10, 12);

        for (int i = 0; i < tableCount; i++) {

            tableRepository.save(
                    RestaurantTable.builder()
                            .tableNumber(i + 1)
                            .capacity(capacities.get(i % capacities.size()))
                            .status(RestaurantTableStatus.AVAILABLE)
                            .restaurant(restaurant)
                            .build()
            );
        }
    }
}
