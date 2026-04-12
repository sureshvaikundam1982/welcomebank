package com.banking.transactionservice.feign;

import com.banking.transactionservice.feign.dto.AccountResponse;
import com.banking.transactionservice.feign.dto.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service", path = "/api/accounts", fallback = AccountServiceClientFallback.class)
public interface AccountServiceClient {

    @GetMapping("/number/{accountNumber}")
    ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber);

    @PutMapping("/balance")
    ResponseEntity<AccountResponse> updateBalance(@RequestBody BalanceUpdateRequest request);
}
