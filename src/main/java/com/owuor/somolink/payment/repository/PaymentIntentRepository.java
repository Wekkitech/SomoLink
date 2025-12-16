package com.owuor.somolink.payment.repository;

import com.owuor.somolink.payment.entity.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

    Optional<PaymentIntent> findByCheckoutRequestId(String checkoutRequestId);
}
