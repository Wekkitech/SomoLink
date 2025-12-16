package com.owuor.somolink.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.owuor.somolink.payment.dto.MpesaCallbackRequest;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.event.PaymentSuccessfulEvent;
import com.owuor.somolink.payment.repository.PaymentIntentRepository;
import com.owuor.somolink.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MpesaCallbackService {

    private final PaymentIntentRepository intentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApplicationEventPublisher publisher;


    public void handleCallback(MpesaCallbackRequest request) throws Exception {

        var callback = request.getBody().getStkCallback();
        String checkoutRequestId = callback.getCheckoutRequestID();

        PaymentIntent intent = intentRepository
                .findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() ->
                        new IllegalStateException("PaymentIntent not found for checkoutId"));

        // 1️⃣ Idempotency / state check
        if (intent.getStatus() != PaymentStatus.STK_ACCEPTED) {
            return;
        }

        // 2️⃣ Handle failure early
        if (callback.getResultCode() != 0) {
            intent.setStatus(PaymentStatus.FAILED);
            intentRepository.save(intent);
            return;
        }

        // 3️⃣ Extract metadata
        BigDecimal paidAmount = null;
        String receipt = null;
        String phone = null;

        if (callback.getCallbackMetadata() != null) {
            for (var item : callback.getCallbackMetadata().getItem()) {
                switch (item.getName()) {
                    case "Amount" -> paidAmount = new BigDecimal(item.getValue().toString());
                    case "MpesaReceiptNumber" -> receipt = item.getValue().toString();
                    case "PhoneNumber" -> phone = item.getValue().toString();
                }
            }
        }

        // 4️⃣ Mandatory metadata validation
        if (paidAmount == null || receipt == null) {
            intent.setStatus(PaymentStatus.FAILED);
            intentRepository.save(intent);
            throw new IllegalStateException("Invalid callback metadata");
        }

        // 5️⃣ Amount integrity check
        if (intent.getAmount().compareTo(paidAmount) != 0) {
            intent.setStatus(PaymentStatus.FAILED);
            intentRepository.save(intent);
            throw new IllegalStateException("Amount mismatch");
        }

        // 6️⃣ Duplicate transaction guard
        if (transactionRepository.existsByCheckoutRequestId(checkoutRequestId)) {
            return;
        }

        // 7️⃣ Mark intent PAID
        intent.setStatus(PaymentStatus.PAID);
        intentRepository.save(intent);

        // 8️⃣ Create transaction
        PaymentTransaction tx = new PaymentTransaction();
        tx.setProfileId(intent.getProfileId());
        tx.setProfileName(intent.getProfileName());
        tx.setPhoneNumber(intent.getPhoneNumber());
        tx.setAmount(paidAmount);
        tx.setMpesaReceiptNumber(receipt);
        tx.setCheckoutRequestId(checkoutRequestId);
        tx.setMerchantRequestId(callback.getMerchantRequestID());
        tx.setStatus(PaymentStatus.PAID);
        tx.setPaidAt(LocalDateTime.now());
        tx.setRawCallback(objectMapper.writeValueAsString(request));

        transactionRepository.save(tx);

        // 9️⃣ Fire provisioning event
        publisher.publishEvent(new PaymentSuccessfulEvent(tx, intent));
    }
}
