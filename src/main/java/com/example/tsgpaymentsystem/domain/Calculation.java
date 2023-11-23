package com.example.tsgpaymentsystem.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "calculation")
public class Calculation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "debt",columnDefinition = "NUMERIC(10,2)")
    private Double debt;
    @Column(name = "outstandingDebt",columnDefinition = "NUMERIC(10,2)")
    private Double outstandingDebt = 0d;
    @ManyToOne
    private Service service;
    @ManyToOne
    private Service group;
    @ManyToOne
    private Account account;
    @ManyToOne
    private Address address;
    @ManyToOne
    private User user;

    @Column(name = "lastUpload")
    private Long lastUpload;
    @Column(name = "lastUploadDate")
    private ZonedDateTime lastUploadDate;
}
