package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountRequest;
import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.BalanceUpdateRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    public AccountResponse createAccount(AccountRequest request, String username) {
        logger.info("Creating {} account for user: {}", request.getAccountType(), username);

        Account account = new Account();
        account.setAccountType(request.getAccountType().toUpperCase());
        account.setBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO);
        account.setUsername(username);
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        account.setStatus("ACTIVE");

        Account saved = accountRepository.save(account);
        logger.info("Account created: {} for user: {}", saved.getAccountNumber(), username);
        return toResponse(saved);
    }

    public List<AccountResponse> getAccountsByUsername(String username) {
        logger.info("Fetching accounts for user: {}", username);
        return accountRepository.findByUsername(username)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        logger.info("Fetching account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return toResponse(account);
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
        return toResponse(account);
    }

    public AccountResponse updateBalance(BalanceUpdateRequest request) {
        logger.info("Updating balance for account: {} type: {} amount: {}",
                request.getAccountNumber(), request.getType(), request.getAmount());

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found: " + request.getAccountNumber()));

        if ("CREDIT".equalsIgnoreCase(request.getType())) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else if ("DEBIT".equalsIgnoreCase(request.getType())) {
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Insufficient funds in account: " + request.getAccountNumber());
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else {
            throw new RuntimeException("Invalid transaction type: " + request.getType());
        }

        Account updated = accountRepository.save(account);
        logger.info("Balance updated for account: {} new balance: {}",
                request.getAccountNumber(), updated.getBalance());
        return toResponse(updated);
    }

    public AccountResponse closeAccount(String accountNumber, String username) {
        logger.info("Closing account: {} for user: {}", accountNumber, username);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        if (!account.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to close this account");
        }

        account.setStatus("CLOSED");
        return toResponse(accountRepository.save(account));
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getUsername(),
                account.getStatus(),
                account.getCurrency(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
