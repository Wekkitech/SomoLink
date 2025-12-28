package com.owuor.somolink.payment.service;

import com.owuor.somolink.network.repository.HotspotUserRepository;
import com.owuor.somolink.payment.dto.PagedPaymentResponse;
import com.owuor.somolink.payment.dto.PaymentVerificationResponse;
import com.owuor.somolink.payment.dto.paymentTransactionResponseDto;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepository;
    private final HotspotUserRepository hotspotUserRepository;

    public List<paymentTransactionResponseDto> getAllPayments() {
        return transactionRepository.findAll()
                .stream()
                .map(tx -> new paymentTransactionResponseDto(
                        tx.getId(),
                        tx.getProfileName(),
                        tx.getPhoneNumber(),
                        tx.getAmount(),
                        tx.getMpesaReceiptNumber(),
                        tx.getStatus(),
                        tx.getPaidAt()
                ))
                .collect(Collectors.toList());
    }


    public PagedPaymentResponse getPayments(
            String search,
            PaymentStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<paymentTransactionResponseDto> txPage =
                transactionRepository.searchTransactions(
                        (search == null || search.isBlank()) ? null : search,
                        status,
                        pageable
                );

        List<paymentTransactionResponseDto> transactions =
                txPage.getContent()
                        .stream()
                        .map(tx ->  new paymentTransactionResponseDto(
                                tx.getId(),
                                tx.getProfileName(),
                                tx.getPhoneNumber(),
                                tx.getAmount(),
                                tx.getMpesaReceiptNumber(),
                                tx.getStatus(),
                                tx.getPaidAt()
                        ))
                        .toList();

        return new PagedPaymentResponse(
                txPage.getTotalElements(),
                transactions
        );
    }

    /**
     * Used when user says:
     * "Nimelipa lakini siwezi login"
     */
    public PaymentVerificationResponse verifyByReceipt(String receipt) {

        PaymentTransaction tx = transactionRepository
                .findByMpesaReceiptNumber(receipt)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found for receipt: " + receipt));

        PaymentVerificationResponse response = new PaymentVerificationResponse();
        response.setPaid(true);
        response.setAmount(tx.getAmount());
        response.setProfileName(tx.getProfileName());
        response.setPaidAt(tx.getPaidAt());

        hotspotUserRepository
                .findByPaymentTransaction(tx)
                .ifPresentOrElse(user -> {
                    response.setUserCreated(true);
                    response.setUsername(user.getUsername());
                    response.setPassword(user.getPassword());
                    response.setLoginUrl(
                            "http://somolink.wifi/login"
                                    + "?username=" + user.getUsername()
                                    + "&password=" + user.getPassword()
                    );
                }, () -> {
                    response.setUserCreated(false);
                });

        return response;
    }
}
