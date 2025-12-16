package com.owuor.somolink.payment.entity;

import com.owuor.somolink.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;
    private String profileName;

    private String phoneNumber;
    private BigDecimal amount;

    private String mpesaReceiptNumber;

    private String checkoutRequestId;
    private String merchantRequestId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawCallback;

    private LocalDateTime paidAt;
}
