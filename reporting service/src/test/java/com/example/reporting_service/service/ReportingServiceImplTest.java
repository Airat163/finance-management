package com.example.reporting_service.service;

import com.example.reporting_service.exception.ReportGenerationException;
import com.example.reporting_service.model.Report;
import com.example.reporting_service.repository.ReportRepository;
import com.example.reporting_service.service.ReportingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportingServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportingServiceImpl reportingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateAccountReport_Success() {
        // URL для accountService
        String accountUrl = "http://account-service/accounts/1";

        // Мок данных аккаунта
        Map<String, Object> mockAccountData = new HashMap<>();
        mockAccountData.put("userId", 1L);  // Необходимое поле userId для успешного теста
        ResponseEntity<Map<String, Object>> accountResponseEntity = ResponseEntity.ok(mockAccountData);

        // Настройка RestTemplate для возврата accountResponseEntity
        when(restTemplate.exchange(
                eq(accountUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(accountResponseEntity);

        // URL для transactionService
        String transactionsUrl = "http://transaction-service/transactions/account/1";

        // Мок данных транзакций
        List<Map<String, Object>> mockTransactionData = new ArrayList<>();
        mockTransactionData.add(new HashMap<>()); // Пример пустого объекта для транзакций
        ResponseEntity<List<Map<String, Object>>> transactionsResponseEntity = ResponseEntity.ok(mockTransactionData);

        // Настройка RestTemplate для возврата transactionsResponseEntity
        when(restTemplate.exchange(
                eq(transactionsUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(transactionsResponseEntity);

        // Мок сохранения в репозитории
        Report mockReport = new Report(1L, "ACCOUNT_REPORT", new Date(), "{}");
        when(reportRepository.save(any(Report.class))).thenReturn(mockReport);

        // Вызов тестируемого метода
        Report report = reportingService.generateAccountReport(1L);

        // Проверка результата
        assertNotNull(report);
        assertEquals("ACCOUNT_REPORT", report.getReportType());
        assertEquals(1L, report.getUserId());
    }


    @Test
    void testGenerateAccountReport_Fallback() {
        // Мокируем вызов RestTemplate для Account, который выбросит исключение
        String accountUrl = "http://account-service/accounts/1";
        when(restTemplate.exchange(
                eq(accountUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Service unavailable"));

        // Проверяем, что метод выбрасывает ReportGenerationException
        assertThrows(ReportGenerationException.class, () -> reportingService.generateAccountReport(1L));
    }
}

