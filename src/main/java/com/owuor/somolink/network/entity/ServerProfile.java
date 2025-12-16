package com.owuor.somolink.network.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ServerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String profileName;

    @Column(nullable = false)
    private String hotspotAddress; // e.g. 192.168.10.1

    @Column(nullable = false)
    private String dnsName;

    private boolean configured; // true if successfully created on MikroTik

    private LocalDateTime createdAt;
}
