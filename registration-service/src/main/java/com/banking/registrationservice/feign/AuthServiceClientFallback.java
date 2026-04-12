package com.banking.registrationservice.feign;

import com.banking.registrationservice.feign.dto.AuthRegisterRequest;
import com.banking.registrationservice.feign.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClientFallback.class);

    @Override
    public ResponseEntity<AuthResponse> register(AuthRegisterRequest request) {
        logger.error("Auth service is unavailable. Fallback triggered for register.");
        AuthResponse fallback = new AuthResponse();
        fallback.setMessage("Auth service is currently unavailable. Please try again later.");
        return ResponseEntity.status(503).body(fallback);
    }
}
