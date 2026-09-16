package com.akhm.restaurantbooking.controller;

import com.akhm.restaurantbooking.dto.user.UserRequest;
import com.akhm.restaurantbooking.dto.user.UserResponse;
import com.akhm.restaurantbooking.entity.User;
import com.akhm.restaurantbooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> findAll() {

        return userService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse findById(
            @PathVariable Long id
    ) {

        return toResponse(
                userService.findById(id)
        );
    }

    @GetMapping("/email")
    public UserResponse findByEmail(
            @RequestParam String email
    ) {

        return toResponse(
                userService.findByEmail(email)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Valid @RequestBody UserRequest request
    ) {

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(request.password())
                .phone(request.phone())
                .build();

        return toResponse(
                userService.save(user)
        );
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(request.password())
                .phone(request.phone())
                .build();

        return toResponse(
                userService.update(id, user)
        );
    }

    @PatchMapping("/{id}/enable")
    public void enable(
            @PathVariable Long id
    ) {

        userService.enable(id);
    }

    @PatchMapping("/{id}/disable")
    public void disable(
            @PathVariable Long id
    ) {

        userService.disable(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {

        userService.delete(id);
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getEnabled(),
                user.getRole() != null
                        ? user.getRole().getName()
                        : null,
                user.getCreatedAt()
        );
    }
}