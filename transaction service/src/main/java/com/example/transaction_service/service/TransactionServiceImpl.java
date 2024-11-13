package com.example.transaction_service.service;

import com.example.account_service.exception.AccountNotFoundException;
import com.example.account_service.model.Account;
import com.example.account_service.service.AccountService;
import com.example.transaction_service.exception.InsufficientFundsException;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.model.TransactionType;
import com.example.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Override
    public Transaction createTransaction(Long accountId, BigDecimal amount, TransactionType type) {
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        if (type == TransactionType.DEBIT && account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transaction");
        }

        Transaction transaction = new Transaction(accountId, amount, type, new Date());
        transactionRepository.save(transaction);

        BigDecimal balanceUpdate = calculateBalanceUpdate(amount, type);
        accountService.updateAccountBalance(accountId, balanceUpdate);

        return transaction;
    }

    private BigDecimal calculateBalanceUpdate(BigDecimal amount, TransactionType type) {
        switch (type) {
            case CREDIT:
            case DEPOSIT:
                return amount;
            case DEBIT:
            case WITHDRAWAL:
                return amount.negate();
            default:
                return BigDecimal.ZERO; // Handle TRANSFER and other types as needed
        }
    }

    @Override
    public Optional<Transaction> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
}
