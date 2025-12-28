package com.owuor.somolink.network.repository;

import com.owuor.somolink.network.entity.HotspotUser;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface HotspotUserRepository extends JpaRepository<HotspotUser, Long> {

    Optional<HotspotUser> findByPaymentTransaction(PaymentTransaction paymentTransaction);

    Optional<HotspotUser> findByPaymentTransaction_Id(Long paymentTransactionId);
}
