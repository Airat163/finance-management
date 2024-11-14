package com.example.reporting_service.service;


import com.example.reporting_service.exception.ReportGenerationException;
import com.example.reporting_service.model.Report;
import com.example.reporting_service.repository.ReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ReportRepository reportRepository;

    private static final String ACCOUNT_SERVICE_URL = "http://account-service/accounts/";
    private static final String TRANSACTION_SERVICE_URL = "http://transaction-service/transactions/account/";

    @Override
    @CircuitBreaker(name = "accountService", fallbackMethod = "accountReportFallback")
    @Retry(name = "accountService")
    public Report generateAccountReport(Long accountId) {
        String accountUrl = ACCOUNT_SERVICE_URL + accountId;
        String transactionsUrl = TRANSACTION_SERVICE_URL + accountId;

        // Получаем данные аккаунта
        ResponseEntity<Map<String, Object>> accountResponse = restTemplate.exchange(
                accountUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> account = accountResponse != null ? accountResponse.getBody() : null;
        if (account == null) {
            throw new ReportGenerationException("Account not found with ID: " + accountId);
        }

        // Получаем данные транзакций
        ResponseEntity<List<Map<String, Object>>> transactionsResponse = restTemplate.exchange(
                transactionsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        List<Map<String, Object>> transactions = transactionsResponse != null ? transactionsResponse.getBody() : Collections.emptyList();

        // Объединяем данные
        Map<String, Object> reportContent = new HashMap<>();
        reportContent.put("account", account);
        reportContent.put("transactions", transactions);

        String reportData = convertToJSON(reportContent);
        Report report = new Report((Long) account.get("userId"), "ACCOUNT_REPORT", new Date(), reportData);

        return reportRepository.save(report);
    }



    @Override
    @CircuitBreaker(name = "transactionService", fallbackMethod = "transactionReportFallback")
    @Retry(name = "transactionService")
    public Report generateTransactionReport(Long accountId, Date fromDate, Date toDate) {
        String transactionsUrl = TRANSACTION_SERVICE_URL + accountId;

        // Получаем все транзакции аккаунта
        List<Map<String, Object>> transactions = restTemplate.exchange(
                transactionsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();

        if (transactions == null || transactions.isEmpty()) {
            throw new ReportGenerationException("No transactions found for account ID: " + accountId);
        }

        // Фильтруем транзакции по дате
        List<Map<String, Object>> filteredTransactions = new ArrayList<>();
        for (Map<String, Object> transaction : transactions) {
            Date createdAt = new Date((Long) transaction.get("createdAt"));
            if (!createdAt.before(fromDate) && !createdAt.after(toDate)) {
                filteredTransactions.add(transaction);
            }
        }

        if (filteredTransactions.isEmpty()) {
            throw new ReportGenerationException("No transactions found in the specified date range.");
        }

        String reportData = convertToJSON(filteredTransactions);
        Report report = new Report(accountId, "TRANSACTION_REPORT", new Date(), reportData);

        return reportRepository.save(report);
    }

    private String convertToJSON(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while converting data to JSON", e);
        }
    }

    // Фолбэки для Circuit Breaker
    public Report accountReportFallback(Long accountId, Throwable throwable) {
        throw new ReportGenerationException("Failed to generate account report: " + throwable.getMessage());
    }

    public Report transactionReportFallback(Long accountId, Date fromDate, Date toDate, Throwable throwable) {
        throw new ReportGenerationException("Failed to generate transaction report: " + throwable.getMessage());
    }
}
