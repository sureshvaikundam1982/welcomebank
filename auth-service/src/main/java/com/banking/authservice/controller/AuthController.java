package com.banking.authservice.controller;

import com.banking.authservice.dto.*;
import com.banking.authservice.entity.User;
import com.banking.authservice.service.JwtService;
import com.banking.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user and get JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration request for username: {}", request.getUsername());
        try {
            AuthResponse response = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request for username: {}", request.getUsername());
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, null, null, "Invalid credentials"));
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Validate a JWT token and return user info")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        logger.debug("Token validation request");
        try {
            if (!jwtService.validateToken(request.getToken())) {
                return ResponseEntity.ok(new TokenValidationResponse(false, null, null, "Invalid or expired token"));
            }
            String username = jwtService.extractUsername(request.getToken());
            String role = jwtService.extractRole(request.getToken());
            return ResponseEntity.ok(new TokenValidationResponse(true, username, role, "Token is valid"));
        } catch (Exception e) {
            return ResponseEntity.ok(new TokenValidationResponse(false, null, null, e.getMessage()));
        }
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve all registered users (Admin only)")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/users/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve user details by username")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        logger.info("Fetching user: {}", username);
        try {
            return ResponseEntity.ok(userService.findByUsername(username));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if auth service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
