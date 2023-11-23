package com.example.tsgpaymentsystem.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

;

@Data
@Entity
@Table(name = "upload_data")
@NoArgsConstructor
public class UploadData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "content")
    @Lob
    private byte[] content;

    private ProcessState state;
    private String status;
    @ManyToOne()
    private User owner;
    @Column(name = "timestamp")
    private ZonedDateTime timestamp;
}
