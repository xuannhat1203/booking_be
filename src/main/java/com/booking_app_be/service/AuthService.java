package com.booking_app_be.service;

import com.booking_app_be.dto.auth.AuthResponse;
import com.booking_app_be.dto.auth.LoginRequest;
import com.booking_app_be.dto.auth.RegisterRequest;
import com.booking_app_be.entity.Role;
import com.booking_app_be.entity.User;
import com.booking_app_be.repository.RoleRepository;
import com.booking_app_be.repository.UserRepository;
import com.booking_app_be.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    private static final String DEFAULT_ROLE_NAME = "customer";

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role role = roleRepository.findByRoleName(DEFAULT_ROLE_NAME)
                .orElseGet(() -> roleRepository.save(Role.builder().roleName(DEFAULT_ROLE_NAME).build()));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        authenticationManager.authenticate(authenticationToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token);
    }
}

