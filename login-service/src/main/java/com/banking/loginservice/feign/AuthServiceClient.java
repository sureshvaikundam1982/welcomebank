package com.banking.loginservice.feign;

import com.banking.loginservice.feign.dto.AuthLoginRequest;
import com.banking.loginservice.feign.dto.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", path = "/api/auth", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody AuthLoginRequest request);
}
