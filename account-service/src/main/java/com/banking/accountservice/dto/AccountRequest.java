package com.banking.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotBlank(message = "Account type is required")
    private String accountType; // SAVINGS, CHECKING, CURRENT

    @PositiveOrZero(message = "Initial balance must be zero or positive")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    private String currency = "USD";
}
