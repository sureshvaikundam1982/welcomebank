package com.banking.registrationservice.feign;

import com.banking.registrationservice.feign.dto.AuthRegisterRequest;
import com.banking.registrationservice.feign.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", path = "/api/auth", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@RequestBody AuthRegisterRequest request);
}
