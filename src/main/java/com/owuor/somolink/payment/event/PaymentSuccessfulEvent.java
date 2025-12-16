package com.owuor.somolink.payment.event;

import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import lombok.Getter;

@Getter
public class PaymentSuccessfulEvent {

    private final PaymentTransaction transaction;
    private final PaymentIntent intent;

    public PaymentSuccessfulEvent(PaymentTransaction transaction , PaymentIntent intent) {
        this.transaction = transaction;
        this.intent = intent;

    }
}
