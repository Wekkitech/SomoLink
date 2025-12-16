package com.owuor.somolink.payment.enums;

public enum PaymentStatus {
    PENDING,        // intent created
    PAID,           // M-Pesa confirmed
    PROVISIONING,   // creating hotspot user
    READY,          // hotspot user created, redirect safe
    FAILED,
    STK_ACCEPTED
}
