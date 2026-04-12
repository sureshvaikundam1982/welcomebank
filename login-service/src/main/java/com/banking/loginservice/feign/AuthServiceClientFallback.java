package com.banking.loginservice.feign;

import com.banking.loginservice.feign.dto.AuthLoginRequest;
import com.banking.loginservice.feign.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClientFallback.class);

    @Override
    public ResponseEntity<AuthResponse> login(AuthLoginRequest request) {
        logger.error("Auth service unavailable. Fallback for login.");
        AuthResponse fallback = new AuthResponse();
        fallback.setMessage("Auth service is currently unavailable. Please try again later.");
        return ResponseEntity.status(503).body(fallback);
    }
}
