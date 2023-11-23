package com.example.tsgpaymentsystem.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "building",uniqueConstraints = {@UniqueConstraint(columnNames = {"building", "user_id"})})
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "building")
    private String building;
    @ManyToOne
    private User user;
}
