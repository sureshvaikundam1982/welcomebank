package com.banking.gateway.filter;

import com.banking.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpMethod;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth-service/api/auth",
            "/registration-service/api/register",
            "/login-service/api/login",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        logger.debug("Incoming request: {} {}", request.getMethod(), path);

        // Always pass CORS preflight requests through without auth check
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (isPublicPath(path)) {
            logger.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            return onAuthError(exchange, "Missing Authorization Header");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            logger.warn("Invalid JWT token for path: {}", path);
            return onAuthError(exchange, "Invalid JWT Token");
        }

        String username = jwtUtil.extractUsername(token);
        logger.debug("Authenticated user: {} for path: {}", username, path);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Auth-Username", username)
                .header("X-Auth-Token", token)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onAuthError(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}
