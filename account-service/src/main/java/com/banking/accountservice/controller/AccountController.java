package com.banking.accountservice.controller;

import com.banking.accountservice.dto.AccountRequest;
import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.BalanceUpdateRequest;
import com.banking.accountservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/accounts")
@Tag(name = "Account Management", description = "Banking Account APIs")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @PostMapping
    @Operation(summary = "Create bank account", description = "Create a new bank account for authenticated user")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-Auth-Username", required = false) String username) {

        if (username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        logger.info("Create account request from user: {}", username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request, username));
    }

    @GetMapping
    @Operation(summary = "Get my accounts", description = "Get all accounts for authenticated user")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @Parameter(hidden = true) @RequestHeader(value = "X-Auth-Username", required = false) String username) {

        if (username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(accountService.getAccountsByUsername(username));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all accounts", description = "Get all accounts (Admin)")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(accountService.getAccountById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        try {
            return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/balance")
    @Operation(summary = "Update account balance", description = "Credit or Debit an account (internal use)")
    public ResponseEntity<AccountResponse> updateBalance(@Valid @RequestBody BalanceUpdateRequest request) {
        try {
            return ResponseEntity.ok(accountService.updateBalance(request));
        } catch (RuntimeException e) {
            logger.error("Balance update error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{accountNumber}/close")
    @Operation(summary = "Close account")
    public ResponseEntity<AccountResponse> closeAccount(
            @PathVariable String accountNumber,
            @Parameter(hidden = true) @RequestHeader(value = "X-Auth-Username", required = false) String username) {

        try {
            return ResponseEntity.ok(accountService.closeAccount(accountNumber, username));
        } catch (RuntimeException e) {
            logger.error("Close account error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Account Service is running");
    }
}
