package com.banking.transactionservice.repository;

import com.banking.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByPerformedBy(String username);
    List<Transaction> findByFromAccountNumber(String accountNumber);
    List<Transaction> findByToAccountNumber(String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccountNumber = :accountNumber OR t.toAccountNumber = :accountNumber")
    List<Transaction> findAllByAccountNumber(String accountNumber);

    List<Transaction> findByStatus(String status);
}
