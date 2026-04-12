package com.banking.transactionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Transaction type is required")
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String fromAccountNumber; // Required for WITHDRAWAL and TRANSFER

    private String toAccountNumber; // Required for DEPOSIT and TRANSFER

    private String description;

    private String currency = "USD";
}
