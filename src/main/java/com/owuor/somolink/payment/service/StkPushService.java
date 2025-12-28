package com.owuor.somolink.payment.service;

import com.owuor.somolink.payment.config.MpesaDarajaProperties;
import com.owuor.somolink.payment.dto.StkPushRequest;
import com.owuor.somolink.payment.dto.StkPushResponse;
import com.owuor.somolink.payment.utils.HelperUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class StkPushService {

    private final MpesaDarajaProperties config;
    private final MpesaAuthService authService;
    private final RestTemplate restTemplate;

    /**
     * Initiates an STK Push request
     *
     * @param phoneNumber in 2547XXXXXXXX format
     * @param amount      numeric string
     * @return StkPushResponse
     */
    public StkPushResponse initiateStkPush(String phoneNumber, String amount) {

        // 1️⃣ Generate timestamp & password
        String timestamp = HelperUtility.getTransactionTimestamp();
        String password = HelperUtility.getStkPushPassword(
                config.getStkPushShortCode(),
                config.getStkPassKey(),
                timestamp
        );

        // 2️⃣ Build STK Push request
        StkPushRequest request = new StkPushRequest();
        request.setBusinessShortCode(config.getStkPushShortCode());
        request.setPassword(password);
        request.setTimestamp(timestamp);
        request.setTransactionType("CustomerPayBillOnline");
        request.setAmount(new BigDecimal(amount).intValue());
        request.setPartyA(phoneNumber);
        request.setPartyB(config.getStkPushShortCode());
        request.setPhoneNumber(phoneNumber);
        request.setCallBackURL(config.getStkPushRequestCallbackUrl());
        request.setAccountReference("SOMOLINK");
        request.setTransactionDesc("Internet Subscription");

        // 3️⃣ Build headers with Bearer token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authService.getAccessToken());

        HttpEntity<StkPushRequest> entity = new HttpEntity<>(request, headers);

        // 4️⃣ Call Safaricom STK Push API
        log.info("Initiating STK Push for phone: {}", phoneNumber);
        ResponseEntity<StkPushResponse> response = restTemplate.postForEntity(
                config.getStkPushRequestUrl(),
                entity,
                StkPushResponse.class
        );

        StkPushResponse body = response.getBody();

        // 5️⃣ Validate response
        if (body == null) {
            log.error("STK Push failed: empty response");
            throw new IllegalStateException("STK Push initiation failed: empty response");
        }

        if (!"0".equals(body.getResponseCode())) {
            log.error("STK Push failed: {}", body);
            throw new IllegalStateException("STK Push initiation failed: " + body.getResponseDescription());
        }

        log.info("STK Push successful, CheckoutRequestID: {}", body.getCheckoutRequestId());
        return body;
    }
}
