package com.owuor.somolink.payment.service;


import com.owuor.somolink.network.entity.HotspotUser;
import com.owuor.somolink.network.repository.HotspotUserRepository;
import com.owuor.somolink.payment.dto.PaymentStatusResponse;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentPollingService {

    private final PaymentIntentRepository intentRepository;
    private final HotspotUserRepository hotspotUserRepository;

    public PaymentStatusResponse getStatus(Long intentId) {

        PaymentIntent intent = intentRepository.findById(intentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found"));

        PaymentTransaction paymentTransaction = intent.getPaymentTransaction();

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setIntentId(intent.getId());
        response.setStatus(intent.getStatus());

        // Only when fully ready
        if (intent.getStatus().name().equals("READY")) {

            HotspotUser user = hotspotUserRepository.findByPaymentTransaction(paymentTransaction).orElseThrow(
                    () -> new IllegalArgumentException("Hotspot user not found")
            );

            response.setUsername(user.getUsername());
            response.setPassword(user.getPassword());

            response.setLoginUrl(
                    "http://somolink.wifi/login"
                            + "?username=" + user.getUsername()
                            + "&password=" + user.getPassword()
            );
        }

        return response;
    }
}
