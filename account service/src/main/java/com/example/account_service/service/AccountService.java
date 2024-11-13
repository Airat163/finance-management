package com.example.account_service.service;

import com.example.account_service.model.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    Account createAccount(Account account);
    Optional<Account> getAccountById(Long accountId);
    Account updateAccountBalance(Long accountId, BigDecimal amount);
    void deleteAccount(Long accountId);
    List<Account> getAllAccounts();
}

