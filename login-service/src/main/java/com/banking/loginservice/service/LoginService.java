package com.banking.loginservice.service;

import com.banking.loginservice.dto.LoginRequest;
import com.banking.loginservice.dto.LoginResponse;
import com.banking.loginservice.feign.AuthServiceClient;
import com.banking.loginservice.feign.dto.AuthLoginRequest;
import com.banking.loginservice.feign.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private AuthServiceClient authServiceClient;

    public LoginResponse login(LoginRequest request) {
        logger.info("Processing login for user: {}", request.getUsername());

        try {
            ResponseEntity<AuthResponse> authResponse = authServiceClient.login(
                    new AuthLoginRequest(request.getUsername(), request.getPassword())
            );

            if (authResponse.getStatusCode().is2xxSuccessful() && authResponse.getBody() != null) {
                AuthResponse body = authResponse.getBody();
                logger.info("Login successful for user: {}", request.getUsername());
                return new LoginResponse(
                        true,
                        body.getToken(),
                        body.getTokenType(),
                        body.getUsername(),
                        body.getEmail(),
                        body.getRole(),
                        "Login successful"
                );
            }

            logger.warn("Login failed for user: {}", request.getUsername());
            return new LoginResponse(false, null, null, null, null, null, "Invalid credentials");

        } catch (Exception e) {
            logger.error("Login error for user {}: {}", request.getUsername(), e.getMessage());
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }
}
