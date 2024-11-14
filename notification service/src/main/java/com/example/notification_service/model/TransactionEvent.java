package com.example.notification_service.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionEvent {
    private Long accountId;
    private BigDecimal amount;
    private String type; // Например, DEPOSIT, WITHDRAWAL и т.д.
}
