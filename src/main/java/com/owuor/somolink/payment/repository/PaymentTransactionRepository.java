package com.owuor.somolink.payment.repository;

import com.owuor.somolink.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {
    boolean existsByCheckoutRequestId(String checkoutRequestId);

     Optional<PaymentTransaction> findByMpesaReceiptNumber(String receipt);
}
