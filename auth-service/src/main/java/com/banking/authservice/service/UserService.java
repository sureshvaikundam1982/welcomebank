package com.banking.authservice.service;

import com.banking.authservice.dto.AuthResponse;
import com.banking.authservice.dto.LoginRequest;
import com.banking.authservice.dto.RegisterRequest;
import com.banking.authservice.entity.User;
import com.banking.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "ROLE_USER");
        user.setEnabled(true);

        userRepository.save(user);
        logger.info("User registered successfully: {}", request.getUsername());

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        logger.info("Login successful for user: {}", request.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
