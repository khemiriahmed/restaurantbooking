package com.akhm.restaurantbooking.controller;

import com.akhm.restaurantbooking.dto.auth.AuthRequest;
import com.akhm.restaurantbooking.dto.auth.AuthResponse;
import com.akhm.restaurantbooking.dto.user.UserRequest;
import com.akhm.restaurantbooking.entity.User;
import com.akhm.restaurantbooking.security.CustomUserDetailsService;
import com.akhm.restaurantbooking.security.JwtService;
import com.akhm.restaurantbooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Connexion.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        User user = userService.findByEmail(request.email());

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(toResponse(user, token));
    }

    /**
     * Inscription (rôle CLIENT par défaut).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody UserRequest request
    ) {

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(request.password())
                .phone(request.phone())
                .build();

        User savedUser = userService.save(user);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(
                        savedUser.getEmail()
                );

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(savedUser, token));
    }

    private AuthResponse toResponse(User user, String token) {

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getRole().getName().name(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
