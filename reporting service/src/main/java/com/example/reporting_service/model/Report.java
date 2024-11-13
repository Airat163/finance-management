package com.example.reporting_service.model;

import com.example.account_service.model.Account;
import com.example.transaction_service.model.Transaction;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "reports")
@NoArgsConstructor
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String reportType;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Lob
    private String data; // JSON или XML данные отчета

    public Report(Long userId, String reportType, Date createdAt, String data) {
        this.userId = userId;
        this.reportType = reportType;
        this.createdAt = createdAt;
        this.data = data;
    }


}
