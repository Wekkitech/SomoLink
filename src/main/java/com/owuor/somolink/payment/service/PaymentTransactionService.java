package com.owuor.somolink.payment.service;

import com.owuor.somolink.network.repository.HotspotUserRepository;
import com.owuor.somolink.payment.dto.PaymentVerificationResponse;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepository;
    private final HotspotUserRepository hotspotUserRepository;

    public List<PaymentTransaction> getAll() {
        return transactionRepository.findAll();
    }

    /**
     * Used when user says:
     * "Nimelipa lakini siwezi login"
     */
    public PaymentVerificationResponse verifyByReceipt(String receipt) {

        PaymentTransaction tx = transactionRepository
                .findByMpesaReceiptNumber(receipt)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found for receipt: " + receipt));

        PaymentVerificationResponse response = new PaymentVerificationResponse();
        response.setPaid(true);
        response.setAmount(tx.getAmount());
        response.setProfileName(tx.getProfileName());
        response.setPaidAt(tx.getPaidAt());

        hotspotUserRepository
                .findByPaymentTransaction(tx)
                .ifPresentOrElse(user -> {
                    response.setUserCreated(true);
                    response.setUsername(user.getUsername());
                    response.setPassword(user.getPassword());
                    response.setLoginUrl(
                            "http://somolink.wifi/login"
                                    + "?username=" + user.getUsername()
                                    + "&password=" + user.getPassword()
                    );
                }, () -> {
                    response.setUserCreated(false);
                });

        return response;
    }
}
