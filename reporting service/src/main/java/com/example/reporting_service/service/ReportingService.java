package com.example.reporting_service.service;

import com.example.reporting_service.model.Report;

import java.util.Date;

public interface ReportingService {
    Report generateAccountReport(Long accountId);
    Report generateTransactionReport(Long accountId, Date fromDate, Date toDate);
}

