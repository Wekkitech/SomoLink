package com.owuor.somolink.school.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false, unique = true)
    private String macAddress;

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_id")
    private School school;

    private LocalDateTime createdAt = LocalDateTime.now();
}
