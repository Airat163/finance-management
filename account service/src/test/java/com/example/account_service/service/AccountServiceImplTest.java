package com.example.account_service.service;

import com.example.account_service.exception.AccountNotFoundException;
import com.example.account_service.model.Account;
import com.example.account_service.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private KafkaTemplate<String, Account> kafkaTemplate;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account(1L, 1L, BigDecimal.valueOf(1000), "USD", new Date());
    }

    @Test
    void testCreateAccount() {
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account createdAccount = accountService.createAccount(testAccount);

        assertNotNull(createdAccount);
        verify(accountRepository, times(1)).save(testAccount);
        verify(kafkaTemplate, times(1)).send(anyString(), eq(testAccount));
    }

    @Test
    void testGetAccountById_Found() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));

        Optional<Account> account = accountService.getAccountById(1L);

        assertTrue(account.isPresent());
        assertEquals(testAccount.getId(), account.get().getId());
    }

    @Test
    void testGetAccountById_NotFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Account> account = accountService.getAccountById(1L);

        assertFalse(account.isPresent());
    }

    @Test
    void testUpdateAccountBalance() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account updatedAccount = accountService.updateAccountBalance(1L, BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(1200), updatedAccount.getBalance());
        verify(accountRepository, times(1)).save(testAccount);
        verify(kafkaTemplate, times(1)).send(anyString(), eq(testAccount));
    }

    @Test
    void testUpdateAccountBalance_NotFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                accountService.updateAccountBalance(1L, BigDecimal.valueOf(200)));
    }

    @Test
    void testDeleteAccount() {
        doNothing().when(accountRepository).deleteById(anyLong());

        accountService.deleteAccount(1L);

        verify(accountRepository, times(1)).deleteById(1L);
        verify(kafkaTemplate, times(1)).send(anyString(), any(Account.class));
    }

    @Test
    void testGetTransactionsForAccount() {
        List<Map<String, Object>> mockTransactions = Collections.singletonList(
                Map.of("transactionId", 1, "amount", BigDecimal.valueOf(100))
        );
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(mockTransactions);

        List<Map<String, Object>> transactions = accountService.getTransactionsForAccount(1L);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(BigDecimal.valueOf(100), transactions.get(0).get("amount"));
    }
}
