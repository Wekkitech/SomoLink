package com.owuor.somolink.payment.controller;

import com.owuor.somolink.payment.dto.PagedPaymentResponse;
import com.owuor.somolink.payment.dto.PaymentInitiateRequest;
import com.owuor.somolink.payment.dto.PaymentStatusResponse;
import com.owuor.somolink.payment.dto.paymentTransactionResponseDto;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.service.PaymentInitiationService;
import com.owuor.somolink.payment.service.PaymentPollingService;
import com.owuor.somolink.payment.service.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    @GetMapping
    public ResponseEntity<PagedPaymentResponse> getPayments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.getPayments(search, status, page, size)
        );
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
