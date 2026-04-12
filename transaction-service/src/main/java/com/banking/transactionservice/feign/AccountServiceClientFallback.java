package com.banking.transactionservice.feign;

import com.banking.transactionservice.feign.dto.AccountResponse;
import com.banking.transactionservice.feign.dto.BalanceUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountServiceClientFallback implements AccountServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceClientFallback.class);

    @Override
    public ResponseEntity<AccountResponse> getAccountByNumber(String accountNumber) {
        logger.error("Account service unavailable. Fallback for getAccountByNumber: {}", accountNumber);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<AccountResponse> updateBalance(BalanceUpdateRequest request) {
        logger.error("Account service unavailable. Fallback for updateBalance.");
        return ResponseEntity.status(503).build();
    }
}
