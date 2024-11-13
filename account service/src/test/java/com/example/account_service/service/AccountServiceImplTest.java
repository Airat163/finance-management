package com.example.account_service.service;

import com.example.account_service.model.Account;
import com.example.account_service.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(1L, BigDecimal.valueOf(1000), "USD", new Date());
    }

    @Test
    void testGetAccountById() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Optional<Account> retrievedAccount = accountService.getAccountById(1L);
        assertTrue(retrievedAccount.isPresent());
        assertEquals(account.getId(), retrievedAccount.get().getId());
    }

    @Test
    void testUpdateAccountBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account updatedAccount = accountService.updateAccountBalance(1L, BigDecimal.valueOf(200));
        assertEquals(BigDecimal.valueOf(1200), updatedAccount.getBalance());
    }
}
