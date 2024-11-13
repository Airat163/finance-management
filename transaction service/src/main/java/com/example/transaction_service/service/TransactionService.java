package com.example.transaction_service.service;

import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Transaction createTransaction(Long accountId, BigDecimal amount, TransactionType type);
    Optional<Transaction> getTransactionById(Long transactionId);
    List<Transaction> getTransactionsByAccountId(Long accountId);
}
