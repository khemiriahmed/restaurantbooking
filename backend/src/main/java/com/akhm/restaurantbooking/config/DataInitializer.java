package com.akhm.restaurantbooking.config;

import com.akhm.restaurantbooking.entity.Role;
import com.akhm.restaurantbooking.enums.RoleName;
import com.akhm.restaurantbooking.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists(RoleName.CLIENT);
        createRoleIfNotExists(RoleName.RESTAURANT);
        createRoleIfNotExists(RoleName.ADMIN);

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
}