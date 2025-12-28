package com.owuor.somolink.payment.service;

import com.owuor.somolink.network.entity.HotspotUser;
import com.owuor.somolink.network.repository.HotspotUserRepository;
import com.owuor.somolink.payment.dto.PaymentStatusResponse;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPollingService {

    private final PaymentIntentRepository intentRepository;
    private final HotspotUserRepository hotspotUserRepository;

    public PaymentStatusResponse getStatus(Long intentId) {

        PaymentIntent intent = intentRepository.findById(intentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found"));
        log.info("Payment intent : {}", intent);

        PaymentTransaction paymentTransaction = intent.getPaymentTransaction();

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setIntentId(intent.getId());
        response.setPaymentStatus(intent.getStatus()); // Payment lifecycle status
        response.setProvisioningStatus(intent.getProvisioningStatus()); // Hotspot provisioning status
        response.setMessage(intent.getStatusMessage());
        response.setCallbackReceived(intent.isCallbackReceived());

        log.info("Payment status: {}", response.getPaymentStatus());
        log.info("Provisioning status: {}", response.getProvisioningStatus().name());
        log.info("payment transaction: {}", paymentTransaction);


        // Only provide login info when provisioning is fully ready
        if (intent.getProvisioningStatus() != null &&
                intent.getProvisioningStatus().name().equals("READY") &&
                paymentTransaction != null) {


            HotspotUser user = hotspotUserRepository
                    .findByPaymentTransaction_Id(paymentTransaction.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Hotspot user not found"));

            log.info("User: {}", user);

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
