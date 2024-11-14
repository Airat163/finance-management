package com.example.account_service.service;

import com.example.account_service.exception.AccountNotFoundException;
import com.example.account_service.model.Account;
import com.example.account_service.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestTemplate restTemplate;
    private static final String ACCOUNT_TOPIC = "account-events";
    private static final String TRANSACTION_SERVICE_URL = "http://transaction-service/transactions/account/";

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, KafkaTemplate<String, String> kafkaTemplate, RestTemplate restTemplate) {
        this.accountRepository = accountRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = restTemplate;
    }

    @Override
    public Account createAccount(Account account) {
        Account savedAccount = accountRepository.save(account);
        kafkaTemplate.send(ACCOUNT_TOPIC, "Account created with ID: " + savedAccount.getId());
        return savedAccount;
    }

    @Override
    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    @Override
    public Account updateAccountBalance(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);
        kafkaTemplate.send(ACCOUNT_TOPIC, "Account updated with ID: " + updatedAccount.getId());
        return updatedAccount;
    }

    @Override
    public void deleteAccount(Long accountId) {
        accountRepository.deleteById(accountId);
        kafkaTemplate.send(ACCOUNT_TOPIC, "Account deleted with ID: " + accountId);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Map<String, Object>> getTransactionsForAccount(Long accountId) {
        String url = TRANSACTION_SERVICE_URL + accountId;
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }
}
