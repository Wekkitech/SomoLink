package com.owuor.somolink.network.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class AccessPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String apName;

    private String schoolName;
    private String macAddress;
    private String model;
    private boolean bridgeMode;
    private String ipAddress;  // optional, for management
    private String description;
    private boolean online;          // ping / AP reachable


    @ManyToOne
    @JoinColumn(name = "hotspot_id", nullable = false)
    private Hotspot hotspot;  // binds this AP to the hotspot configured on an interface
}
