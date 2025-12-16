package com.owuor.somolink.network.entity;

import com.owuor.somolink.school.entity.School;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class PortConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String schoolName;      // Optional: identify the school
    private String portName;        // Ether5, ether2-hotspot etc.

    private String cidr;            // Example: 192.168.10.1/24
    private int subnetMask;         // Example: 24
    private String networkCidr;     // Example: 192.168.10.0/24

    private String dhcpPoolRange;   // Example: 192.168.10.2-192.168.10.254
    private String dhcpPoolName;    // Example: pool_ether5_a8d9f1

    private String description;     // Optional label

    private boolean configured;     // Whether it was successfully applied
    private LocalDateTime configuredAt; // Timestamp of router push

    @OneToOne
    @JoinColumn(name = "school_id")
    private School school;

    // Optional: back-reference to hotspot (ONE-TO-ONE)
    @OneToOne(mappedBy = "portConfiguration")
    private Hotspot hotspot;
}
