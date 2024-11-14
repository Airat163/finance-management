package com.example.transaction_service.service;

import com.example.account_service.model.Account;
import com.example.transaction_service.exception.AccountNotFoundException;
import com.example.transaction_service.exception.InsufficientFundsException;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.model.TransactionType;
import com.example.transaction_service.repository.TransactionRepository;
import com.example.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;  // Взаимодействие через RestTemplate
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private static final String ACCOUNT_TOPIC = "account-events";
    private static final String ACCOUNT_SERVICE_URL = "http://account-service/accounts/";

    @Override
    public Transaction createTransaction(Long accountId, BigDecimal amount, TransactionType type) {
        // Получаем информацию о счете с помощью RestTemplate
        String url = ACCOUNT_SERVICE_URL + accountId;
        Account account = restTemplate.getForObject(url, Account.class);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        if (type == TransactionType.DEBIT && account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transaction");
        }

        // Создаем и сохраняем транзакцию
        Transaction transaction = new Transaction(accountId, amount, type, new Date());
        transactionRepository.save(transaction);

        // Обновляем баланс счета через Account Service
        BigDecimal balanceUpdate = calculateBalanceUpdate(amount, type);
        restTemplate.put(ACCOUNT_SERVICE_URL + accountId + "/balance?amount=" + balanceUpdate, null);

        // Публикуем событие транзакции в Kafka
        kafkaTemplate.send(ACCOUNT_TOPIC, transaction);

        return transaction;
    }

    private BigDecimal calculateBalanceUpdate(BigDecimal amount, TransactionType type) {
        return switch (type) {
            case CREDIT, DEPOSIT -> amount;
            case DEBIT, WITHDRAWAL -> amount.negate();
            default -> BigDecimal.ZERO; // Дополнительная обработка для других типов
        };
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
