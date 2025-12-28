package com.owuor.somolink.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.owuor.somolink.payment.dto.MpesaCallbackRequest;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.event.PaymentSuccessfulEvent;
import com.owuor.somolink.payment.repository.PaymentIntentRepository;
import com.owuor.somolink.payment.repository.PaymentTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaCallbackService {

    private final PaymentIntentRepository intentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void handleCallback(MpesaCallbackRequest request) throws Exception {

        // Null-safe check
        if (request.getBody() == null || request.getBody().getStkCallback() == null) {
            log.warn("Empty callback body received: {}", objectMapper.writeValueAsString(request));
            return; // Safely ignore
        }

        var callback = request.getBody().getStkCallback();
        String checkoutRequestId = callback.getCheckoutRequestID();
        int resultCode = callback.getResultCode();
        String resultDesc = callback.getResultDesc();

        PaymentIntent intent = intentRepository
                .findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new IllegalStateException("PaymentIntent not found for checkoutId: " + checkoutRequestId));

        // Idempotency check
        if (intent.getStatus() != PaymentStatus.STK_ACCEPTED) {
            log.info("PaymentIntent already processed or not in STK_ACCEPTED state, skipping: {}", checkoutRequestId);
            return;
        }

        // Handle failure or user cancel
        if (resultCode != 0) {
            log.warn("STK Push failed for {}: {} ({})", checkoutRequestId, resultDesc, resultCode);
            intent.setStatusMessage(resultDesc); // New field in PaymentIntent entity (String)
            intent.setCallbackReceived(true);

            intent.setStatus(PaymentStatus.FAILED);
            intentRepository.save(intent);

            // Save failed transaction
            PaymentTransaction failedTx = new PaymentTransaction();
            failedTx.setProfileId(intent.getProfileId());
            failedTx.setProfileName(intent.getProfileName());
            failedTx.setPhoneNumber(intent.getPhoneNumber());
            failedTx.setCheckoutRequestId(checkoutRequestId);
            failedTx.setAmount(intent.getAmount());
            failedTx.setMerchantRequestId(callback.getMerchantRequestID());
            failedTx.setStatus(PaymentStatus.FAILED);
            failedTx.setRawCallback(objectMapper.writeValueAsString(request));
            failedTx.setPaidAt(LocalDateTime.now());

            transactionRepository.save(failedTx);
            return;
        }

        // Extract metadata (only present on success)
        BigDecimal paidAmount = null;
        String receipt = null;
        String phone = null;

        if (callback.getCallbackMetadata() != null && callback.getCallbackMetadata().getItem() != null) {
            for (var item : callback.getCallbackMetadata().getItem()) {
                switch (item.getName()) {
                    case "Amount" -> paidAmount = new BigDecimal(item.getValue().toString());
                    case "MpesaReceiptNumber" -> receipt = item.getValue().toString();
                    case "PhoneNumber" -> phone = item.getValue().toString();
                }
            }
        }

        // Validate mandatory metadata
        if (paidAmount == null || receipt == null) {
            log.error("Callback metadata missing for {}", checkoutRequestId);
            intent.setCallbackReceived(true);
            intent.setStatus(PaymentStatus.FAILED);
            intentRepository.save(intent);
            throw new IllegalStateException("Invalid callback metadata for checkoutRequestId: " + checkoutRequestId);
        }

        // Amount integrity check
        if (intent.getAmount().compareTo(paidAmount) != 0) {
            log.error("Paid amount mismatch: expected {} but got {} for checkoutRequestId {}", intent.getAmount(), paidAmount, checkoutRequestId);
            intent.setStatus(PaymentStatus.FAILED);
            intent.setCallbackReceived(true);
            intentRepository.save(intent);
            throw new IllegalStateException("Amount mismatch for checkoutRequestId: " + checkoutRequestId);
        }

        // Prevent duplicate transaction
        if (transactionRepository.existsByCheckoutRequestId(checkoutRequestId)) {
            log.info("Duplicate transaction detected for checkoutRequestId {}, skipping", checkoutRequestId);
            return;
        }

        // Mark intent as PAID
        intent.setStatus(PaymentStatus.PAID);
        intent.setCallbackReceived(true);
        intent.setStatusMessage(resultDesc);
        intentRepository.save(intent);

        // Save successful transaction
        PaymentTransaction tx = new PaymentTransaction();
        tx.setProfileId(intent.getProfileId());
        tx.setProfileName(intent.getProfileName());
        tx.setPhoneNumber(phone);
        tx.setAmount(paidAmount);
        tx.setMpesaReceiptNumber(receipt);
        tx.setCheckoutRequestId(checkoutRequestId);
        tx.setMerchantRequestId(callback.getMerchantRequestID());
        tx.setStatus(PaymentStatus.PAID);
        tx.setPaidAt(LocalDateTime.now());
        tx.setRawCallback(objectMapper.writeValueAsString(request));

        PaymentTransaction ptx = transactionRepository.save(tx);
        intent.setPaymentTransaction(ptx);


        // Publish event for provisioning
        publisher.publishEvent(new PaymentSuccessfulEvent(tx, intent));

        log.info("STK Push callback processed successfully for checkoutRequestId {}", checkoutRequestId);
    }
}
