package com.banking.registrationservice.controller;

import com.banking.registrationservice.dto.RegistrationRequest;
import com.banking.registrationservice.dto.RegistrationResponse;
import com.banking.registrationservice.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@Tag(name = "Registration", description = "User Registration APIs")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private RegistrationService registrationService;

    @PostMapping
    @Operation(summary = "Register new user", description = "Register a new banking user")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        logger.info("Registration request received for: {}", request.getUsername());
        try {
            RegistrationResponse response = registrationService.registerUser(request);
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RegistrationResponse(false, e.getMessage(), null, null, null));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Registration Service is running");
    }
}
