package com.owuor.somolink.payment.controller;

import com.owuor.somolink.payment.dto.PaymentInitiateRequest;
import com.owuor.somolink.payment.dto.PaymentStatusResponse;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.service.PaymentInitiationService;
import com.owuor.somolink.payment.service.PaymentPollingService;
import com.owuor.somolink.payment.service.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentInitiationService paymentInitiationService;
    private final PaymentTransactionService paymentTransactionService;
    private final PaymentPollingService pollingService;


    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request
    ) {
        PaymentIntent intent = paymentInitiationService.initiatePayment(request);
        return ResponseEntity.ok(intent);
    }

    @GetMapping("/{id}/status")
    public PaymentStatusResponse poll(@PathVariable Long id) {
        return pollingService.getStatus(id);
    }

    // 🔎 1. Get all payment transactions (admin / support)
    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions() {
        return ResponseEntity.ok(paymentTransactionService.getAll());
    }

    // 🔎 2. Verify payment by M-Pesa receipt (customer support flow)
    @GetMapping("/verify/{mpesaReceipt}")
    public ResponseEntity<?> verifyByReceipt(
            @PathVariable String mpesaReceipt
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.verifyByReceipt(mpesaReceipt)
        );
    }


}
