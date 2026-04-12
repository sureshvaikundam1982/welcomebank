package com.banking.registrationservice.service;

import com.banking.registrationservice.dto.RegistrationRequest;
import com.banking.registrationservice.dto.RegistrationResponse;
import com.banking.registrationservice.feign.AuthServiceClient;
import com.banking.registrationservice.feign.dto.AuthRegisterRequest;
import com.banking.registrationservice.feign.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    @Autowired
    private AuthServiceClient authServiceClient;

    public RegistrationResponse registerUser(RegistrationRequest request) {
        logger.info("Processing registration for user: {}", request.getUsername());

        AuthRegisterRequest authRequest = new AuthRegisterRequest(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                "ROLE_USER"
        );

        try {
            ResponseEntity<AuthResponse> authResponse = authServiceClient.register(authRequest);

            if (authResponse.getStatusCode().is2xxSuccessful() && authResponse.getBody() != null) {
                AuthResponse body = authResponse.getBody();
                logger.info("Registration successful for user: {}", request.getUsername());
                return new RegistrationResponse(
                        true,
                        "Registration successful! Welcome " + request.getFirstName(),
                        body.getToken(),
                        body.getUsername(),
                        body.getEmail()
                );
            } else {
                logger.error("Registration failed for user: {}", request.getUsername());
                return new RegistrationResponse(false, "Registration failed", null, null, null);
            }
        } catch (Exception e) {
            logger.error("Registration error for user {}: {}", request.getUsername(), e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
}
