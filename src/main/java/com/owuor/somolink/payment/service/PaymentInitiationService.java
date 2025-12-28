package com.owuor.somolink.payment.service;

import com.owuor.somolink.network.entity.UserProfile;
import com.owuor.somolink.network.repository.UserProfileRepository;
import com.owuor.somolink.payment.dto.PaymentInitiateRequest;
import com.owuor.somolink.payment.dto.StkPushResponse;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentInitiationService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final UserProfileRepository userProfileRepository;
    private final StkPushService stkPushService;

    public PaymentIntent initiatePayment(PaymentInitiateRequest request) {
        // 1️⃣ Fetch user
        UserProfile userProfile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User profile not found with id: " + request.getProfileId()));

        // 2️⃣ Create PaymentIntent
        PaymentIntent intent = new PaymentIntent();
        intent.setPhoneNumber(request.getPhoneNumber());
        intent.setAmount(userProfile.getAmount());
        intent.setProfileId(userProfile.getId());
        intent.setProfileName(userProfile.getProfileName());
        intent.setMacAddress(request.getMacAddress());
        intent.setStatus(PaymentStatus.PENDING);
        intent.setCreatedAt(LocalDateTime.now());

        // 3️⃣ Persist PENDING intent
        PaymentIntent savedIntent = paymentIntentRepository.save(intent);

        // 4️⃣ Trigger STK Push asynchronously
        initiateStkPushAsync(savedIntent);

        return savedIntent;
    }

    /**
     * Triggers the STK Push in a separate thread to avoid blocking the UI.
     */
    @Async
    public void initiateStkPushAsync(PaymentIntent intent) {
        try {
            log.info("Starting STK Push for PaymentIntent id={}", intent.getId());

            // Call STK Push service
            StkPushResponse response = stkPushService.initiateStkPush(
                    intent.getPhoneNumber(),
                    intent.getAmount().toPlainString()
            );

            // Update intent with STK ACCEPTED info
            intent.setCheckoutRequestId(response.getCheckoutRequestId());
            intent.setMerchantRequestId(response.getMerchantRequestId());
            intent.setStatus(PaymentStatus.STK_ACCEPTED);

            paymentIntentRepository.save(intent);

            log.info("STK Push initiated successfully for PaymentIntent id={}", intent.getId());

        } catch (Exception e) {
            log.error("STK Push failed for PaymentIntent id={}, error={}", intent.getId(), e.getMessage());

            // Mark the intent as FAILED on error
            intent.setStatus(PaymentStatus.FAILED);
            paymentIntentRepository.save(intent);
        }
    }
}
