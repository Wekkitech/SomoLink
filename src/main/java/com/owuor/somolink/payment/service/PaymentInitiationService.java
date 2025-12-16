package com.owuor.somolink.payment.service;

import com.owuor.somolink.auth.repository.UserRepository;
import com.owuor.somolink.network.entity.UserProfile;
import com.owuor.somolink.network.repository.UserProfileRepository;
import com.owuor.somolink.payment.dto.PaymentInitiateRequest;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentInitiationService {

    private final PaymentIntentRepository paymentIntentRepository;

    private final UserProfileRepository userProfileRepository;

    public PaymentIntent initiatePayment(PaymentInitiateRequest request) {

        UserProfile userProfile = userProfileRepository.findById(request.getProfileId()).orElseThrow(
                () -> new IllegalArgumentException("Hotspot not found with id: " + request.getProfileId())
        );

        PaymentIntent intent = new PaymentIntent();
        intent.setPhoneNumber(request.getPhoneNumber());
        intent.setProfileId(userProfile.getId());
        intent.setProfileName(userProfile.getProfileName());

        intent.setMacAddress(request.getMacAddress());
        intent.setStatus(PaymentStatus.PENDING);
        intent.setCreatedAt(LocalDateTime.now());

        PaymentIntent saved = paymentIntentRepository.save(intent);

        // 🔜 NEXT STEP:
        // Call MPESA STK PUSH here
        // Then update:

         saved.setCheckoutRequestId("12345-ABCDE");
         saved.setMerchantRequestId("ws_CO_1234567890");

         saved.setStatus(PaymentStatus.STK_ACCEPTED);

         paymentIntentRepository.save(saved);
        return saved;
    }
}
