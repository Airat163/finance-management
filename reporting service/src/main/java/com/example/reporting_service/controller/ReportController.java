package com.example.reporting_service.controller;

import com.example.reporting_service.model.Report;
import com.example.reporting_service.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportingService reportingService;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Report> generateAccountReport(@PathVariable Long accountId) {
        Report report = reportingService.generateAccountReport(accountId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Report> generateTransactionReport(
            @RequestParam Long accountId,
            @RequestParam Date fromDate,
            @RequestParam Date toDate) {
        Report report = reportingService.generateTransactionReport(accountId, fromDate, toDate);
        return ResponseEntity.ok(report);
    }
}
