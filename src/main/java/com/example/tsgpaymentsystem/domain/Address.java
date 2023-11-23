package com.example.tsgpaymentsystem.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "address",uniqueConstraints = {@UniqueConstraint(columnNames = {"building_id", "user_id", "apartment"})})
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "apartment")
    private String apartment;
    @ManyToOne
    private Account account;
    @OneToOne(optional = true)
    private Building building;
    @ManyToOne
    private User user;
}
