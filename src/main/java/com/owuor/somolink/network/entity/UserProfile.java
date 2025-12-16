package com.owuor.somolink.network.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String profileName;

    private int rateLimitUpload;          // kbps
    private int rateLimitDownload;        // kbps
    private String sessionTimeout;        // e.g., "1h"
    private String idleTimeout;           // e.g., "10m"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private String description;
}
