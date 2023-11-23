package com.example.tsgpaymentsystem.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "payment",indexes = {@Index(name = "payment_timestamp_index", columnList = "timestamp")})
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "NUMERIC(10,2)")
    private Double payment;
    @Column(nullable = false, columnDefinition = "NUMERIC(10,2)")
    private Double outstandingDebt = 0d;
    @Column(nullable = false)
    private ZonedDateTime timestamp;
    @ManyToOne(optional = false)
    private Service service;

    @ManyToOne(optional = false)
    private Account account;
    @ManyToOne(optional = false)
    private Address address;

    @ManyToOne
    private User user;

    private Long lastUpload;
}

