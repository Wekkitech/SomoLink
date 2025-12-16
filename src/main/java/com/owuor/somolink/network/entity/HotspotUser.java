package com.owuor.somolink.network.entity;

import com.owuor.somolink.payment.entity.PaymentTransaction;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "hotspot_users")
public class HotspotUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String profileName;

    private String macAddress;

    private LocalDateTime startsAt;

    private LocalDateTime expiresAt;

    @OneToOne
    @JoinColumn(name = "payment_transaction_id", nullable = false)
    private PaymentTransaction paymentTransaction;

    private boolean active;
}
