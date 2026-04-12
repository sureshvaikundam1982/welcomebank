package com.banking.transactionservice.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String type; // CREDIT or DEBIT
}
