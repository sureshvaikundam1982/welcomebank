package com.banking.transactionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String status;
    private String description;
    private String currency;
    private String performedBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
