package com.owuor.somolink.payment.entity;

import com.owuor.somolink.payment.enums.ProvisioningStatus;
import com.owuor.somolink.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_intents")
public class PaymentIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Long profileId;

    @Column(nullable = false)
    private String profileName;

    private String macAddress;

    // Returned by Safaricom after STK push
    @Column(unique = true)
    private String checkoutRequestId;

    private String merchantRequestId;

    private String statusMessage;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "payment_transaction_id")
    private PaymentTransaction paymentTransaction;

    @Column(name = "callback_received", nullable = true)
    private boolean callbackReceived = false;

    @Enumerated(EnumType.STRING)
    private ProvisioningStatus provisioningStatus = ProvisioningStatus.PENDING;

}
