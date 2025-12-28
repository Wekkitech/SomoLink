package com.owuor.somolink.network.consumer;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.entity.HotspotUser;
import com.owuor.somolink.network.repository.HotspotUserRepository;
import com.owuor.somolink.payment.enums.ProvisioningStatus;
import com.owuor.somolink.payment.entity.PaymentIntent;
import com.owuor.somolink.payment.event.PaymentSuccessfulEvent;
import com.owuor.somolink.payment.repository.PaymentIntentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotspotProvisioningListener {

    private final RouterOSClient routerOSClient;
    private final HotspotUserRepository repo;
    private final PaymentIntentRepository paymentIntentRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void provision(PaymentSuccessfulEvent event) {
        try {
            log.info("Provisioning hotspot for tx {}", event.getTransaction().getId());

            var tx = event.getTransaction();
            var intent = event.getIntent();

            intent.setProvisioningStatus(ProvisioningStatus.PROVISIONING);
            paymentIntentRepository.save(intent);

            String username = "hs-" + UUID.randomUUID().toString().substring(0, 8);
            String password = UUID.randomUUID().toString().substring(0, 10);

            routerOSClient.createHotspotUser(username, password, tx.getProfileName());

            HotspotUser user = new HotspotUser();
            user.setUsername(username);
            user.setPassword(password);
            user.setProfileName(tx.getProfileName());
            user.setStartsAt(LocalDateTime.now());
            user.setActive(true);
            user.setPaymentTransaction(tx);
            repo.save(user);

            intent.setProvisioningStatus(ProvisioningStatus.READY);
            paymentIntentRepository.save(intent);

            log.info("Hotspot provisioning completed for tx {}", tx.getId());

        } catch (Exception e) {
            log.error("Provisioning failed", e.getMessage());
        }
    }
}
