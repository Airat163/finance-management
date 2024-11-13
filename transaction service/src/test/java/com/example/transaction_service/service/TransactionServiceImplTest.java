package com.example.transaction_service.service;

import com.example.account_service.model.Account;
import com.example.account_service.service.AccountService;
import com.example.transaction_service.exception.InsufficientFundsException;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.model.TransactionType;
import com.example.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionServiceImpl transactionService;


    @Test
    void testCreateTransaction_Credit() {
        Account account = new Account(1L, BigDecimal.valueOf(1000), "USD", new Date());
        when(accountService.getAccountById(anyLong())).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction(1L, BigDecimal.valueOf(500), TransactionType.CREDIT, new Date()));

        Transaction transaction = transactionService.createTransaction(1L, BigDecimal.valueOf(500), TransactionType.CREDIT);

        assertNotNull(transaction);
        assertEquals(TransactionType.CREDIT, transaction.getType());
        verify(accountService, times(1)).updateAccountBalance(1L, BigDecimal.valueOf(500));
    }

    @Test
    void testCreateTransaction_Debit_InsufficientFunds() {
        Account account = new Account(1L, BigDecimal.valueOf(100), "USD", new Date());
        when(accountService.getAccountById(anyLong())).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () ->
                transactionService.createTransaction(1L, BigDecimal.valueOf(500), TransactionType.DEBIT));
    }

    @Test
    void testGetTransactionById() {
        Transaction transaction = new Transaction(1L, BigDecimal.valueOf(500), TransactionType.CREDIT, new Date());
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));

        Optional<Transaction> foundTransaction = transactionService.getTransactionById(1L);

        assertTrue(foundTransaction.isPresent());
        assertEquals(TransactionType.CREDIT, foundTransaction.get().getType());
    }
}
