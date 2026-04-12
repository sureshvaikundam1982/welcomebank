package com.banking.transactionservice.service;

import com.banking.transactionservice.dto.TransactionRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.feign.AccountServiceClient;
import com.banking.transactionservice.feign.dto.AccountResponse;
import com.banking.transactionservice.feign.dto.BalanceUpdateRequest;
import com.banking.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountServiceClient accountServiceClient;

    public TransactionResponse processTransaction(TransactionRequest request, String username) {
        logger.info("Processing {} transaction of {} by user: {}",
                request.getType(), request.getAmount(), username);

        Transaction transaction = new Transaction();
        transaction.setType(request.getType().toUpperCase());
        transaction.setAmount(request.getAmount());
        transaction.setFromAccountNumber(request.getFromAccountNumber());
        transaction.setToAccountNumber(request.getToAccountNumber());
        transaction.setDescription(request.getDescription());
        transaction.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        transaction.setPerformedBy(username);
        transaction.setStatus("PENDING");

        transaction = transactionRepository.save(transaction);

        try {
            switch (request.getType().toUpperCase()) {
                case "DEPOSIT" -> processDeposit(request);
                case "WITHDRAWAL" -> processWithdrawal(request);
                case "TRANSFER" -> processTransfer(request);
                default -> throw new RuntimeException("Unknown transaction type: " + request.getType());
            }

            transaction.setStatus("COMPLETED");
            transaction.setCompletedAt(LocalDateTime.now());
            logger.info("Transaction {} completed successfully", transaction.getTransactionId());

        } catch (Exception e) {
            transaction.setStatus("FAILED");
            logger.error("Transaction {} failed: {}", transaction.getTransactionId(), e.getMessage());
            transaction = transactionRepository.save(transaction);
            throw new RuntimeException("Transaction failed: " + e.getMessage());
        }

        return toResponse(transactionRepository.save(transaction));
    }

    private void processDeposit(TransactionRequest request) {
        if (request.getToAccountNumber() == null) {
            throw new RuntimeException("Destination account required for deposit");
        }
        validateAccountExists(request.getToAccountNumber());
        updateBalance(request.getToAccountNumber(), request.getAmount(), "CREDIT");
        logger.info("Deposited {} to account {}", request.getAmount(), request.getToAccountNumber());
    }

    private void processWithdrawal(TransactionRequest request) {
        if (request.getFromAccountNumber() == null) {
            throw new RuntimeException("Source account required for withdrawal");
        }
        validateAccountExists(request.getFromAccountNumber());
        updateBalance(request.getFromAccountNumber(), request.getAmount(), "DEBIT");
        logger.info("Withdrew {} from account {}", request.getAmount(), request.getFromAccountNumber());
    }

    private void processTransfer(TransactionRequest request) {
        if (request.getFromAccountNumber() == null || request.getToAccountNumber() == null) {
            throw new RuntimeException("Both source and destination accounts required for transfer");
        }
        validateAccountExists(request.getFromAccountNumber());
        validateAccountExists(request.getToAccountNumber());
        updateBalance(request.getFromAccountNumber(), request.getAmount(), "DEBIT");
        updateBalance(request.getToAccountNumber(), request.getAmount(), "CREDIT");
        logger.info("Transferred {} from {} to {}", request.getAmount(),
                request.getFromAccountNumber(), request.getToAccountNumber());
    }

    private void validateAccountExists(String accountNumber) {
        ResponseEntity<AccountResponse> response = accountServiceClient.getAccountByNumber(accountNumber);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Account not found: " + accountNumber);
        }
        AccountResponse account = response.getBody();
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active: " + accountNumber);
        }
    }

    private void updateBalance(String accountNumber, java.math.BigDecimal amount, String type) {
        ResponseEntity<AccountResponse> response = accountServiceClient.updateBalance(
                new BalanceUpdateRequest(accountNumber, amount, type)
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to update balance for account: " + accountNumber);
        }
    }

    public List<TransactionResponse> getTransactionsByUsername(String username) {
        return transactionRepository.findByPerformedBy(username)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        return transactionRepository.findAllByAccountNumber(accountNumber)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return toResponse(transaction);
    }

    public TransactionResponse getTransactionByTxnId(String txnId) {
        Transaction transaction = transactionRepository.findByTransactionId(txnId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + txnId));
        return toResponse(transaction);
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getFromAccountNumber(),
                transaction.getToAccountNumber(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCurrency(),
                transaction.getPerformedBy(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}
