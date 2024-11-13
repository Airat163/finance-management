package com.example.reporting_service.service;


import com.example.account_service.model.Account;
import com.example.account_service.service.AccountService;
import com.example.reporting_service.exception.ReportGenerationException;
import com.example.reporting_service.model.Report;
import com.example.reporting_service.exception.AccountNotFoundException;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.reporting_service.repository.ReportRepository;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportingServiceImpl implements ReportingService {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final ObjectMapper objectMapper;
    private final ReportRepository reportRepository;

    @Override
    public Report generateAccountReport(Long accountId) {
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);

        // Объединение данных аккаунта и транзакций
        Map<String, Object> reportContent = new HashMap<>();
        reportContent.put("account", account);
        reportContent.put("transactions", transactions);

        String reportData = convertToJSON(reportContent);
        Report report = new Report(account.getUserId(), "ACCOUNT_REPORT", new Date(), reportData);

        return reportRepository.save(report); // Сохранение отчета в базе данных
    }

    @Override
    public Report generateTransactionReport(Long accountId, Date fromDate, Date toDate) {
        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId)
                .stream()
                .filter(t -> !t.getCreatedAt().before(fromDate) && !t.getCreatedAt().after(toDate))
                .collect(Collectors.toList());

        if (transactions.isEmpty()) {
            throw new ReportGenerationException("No transactions found for the specified date range.");
        }

        String reportData = convertToJSON(transactions);
        Report report = new Report(accountId, "TRANSACTION_REPORT", new Date(), reportData);

        return reportRepository.save(report); // Сохранение отчета в базе данных
    }

    private String convertToJSON(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while converting data to JSON", e);
        }
    }
}



