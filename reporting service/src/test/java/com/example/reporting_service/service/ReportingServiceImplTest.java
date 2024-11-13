package com.example.reporting_service.service;

import com.example.account_service.model.Account;
import com.example.account_service.service.AccountService;
import com.example.reporting_service.model.Report;
import com.example.reporting_service.repository.ReportRepository;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ReportingServiceImplTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReportRepository reportRepository; // Добавляем мок для ReportRepository

    @InjectMocks
    private ReportingServiceImpl reportingService;

    @Test
    void testGenerateAccountReport() throws Exception {
        Account account = new Account(1L, 1L, BigDecimal.valueOf(1000), "USD", new Date());
        when(accountService.getAccountById(anyLong())).thenReturn(Optional.of(account));
        when(transactionService.getTransactionsByAccountId(anyLong())).thenReturn(Collections.emptyList());
        when(objectMapper.writeValueAsString(any())).thenReturn("test_data");

        // Добавьте поведение для save, если оно потребуется
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Report report = reportingService.generateAccountReport(1L);

        assertNotNull(report);
        verify(accountService, times(1)).getAccountById(1L);
        verify(transactionService, times(1)).getTransactionsByAccountId(1L);
        verify(reportRepository, times(1)).save(any(Report.class));
    }
}
