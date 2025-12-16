package com.owuor.somolink.network.consumer;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.entity.HotspotUser;
import com.owuor.somolink.network.repository.HotspotUserRepository;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.event.PaymentSuccessfulEvent;
import com.owuor.somolink.payment.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HotspotProvisioningListener {

    private final RouterOSClient routerOSClient;
    private final HotspotUserRepository repo;
    private final PaymentIntentRepository intentRepository;
    private final PaymentIntentRepository paymentIntentRepository;

    @Async
    @TransactionalEventListener
    public void provision(PaymentSuccessfulEvent event) throws Exception {

        var tx = event.getTransaction();
        PaymentIntent paymentIntent = event.getIntent();

        String username = "hs-" + UUID.randomUUID().toString().substring(0, 8);
        String password = UUID.randomUUID().toString().substring(0, 10);

        paymentIntent.setStatus(PaymentStatus.PROVISIONING);
        paymentIntentRepository.save(paymentIntent);

        routerOSClient.createHotspotUser(
                username,
                password,
                tx.getProfileName(),
                "somolink.wifi"
        );

        HotspotUser user = new HotspotUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setProfileName(String.valueOf(tx.getProfileId()));
        user.setStartsAt(LocalDateTime.now());
        user.setActive(true);
        user.setPaymentTransaction(tx);


        paymentIntent.setStatus(PaymentStatus.READY);
        paymentIntentRepository.save(paymentIntent);

        repo.save(user);
    }
}
