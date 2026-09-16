package com.akhm.restaurantbooking.service;

import com.akhm.restaurantbooking.entity.Role;
import com.akhm.restaurantbooking.entity.User;
import com.akhm.restaurantbooking.enums.RoleName;
import com.akhm.restaurantbooking.exception.ConflictException;
import com.akhm.restaurantbooking.exception.ResourceNotFoundException;
import com.akhm.restaurantbooking.repository.RoleRepository;
import com.akhm.restaurantbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );
    }

    public User save(User user) {

        if (user.getEmail() != null
                && userRepository.existsByEmail(user.getEmail())) {

            throw new ConflictException(
                    "Email already exists: " + user.getEmail()
            );
        }

        Role clientRole = roleRepository.findByName(RoleName.CLIENT)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CLIENT role not found"
                        )
                );

        user.setRole(clientRole);

        return userRepository.save(user);
    }

    public User update(Long id, User updatedUser) {

        User existingUser = findById(id);

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setEnabled(updatedUser.getEnabled());

        return userRepository.save(existingUser);
    }

    public void delete(Long id) {

        User user = findById(id);

        userRepository.delete(user);
    }

    public void enable(Long id) {

        User user = findById(id);

        user.setEnabled(true);

        userRepository.save(user);
    }

    public void disable(Long id) {

        User user = findById(id);

        user.setEnabled(false);

        userRepository.save(user);
    }
}