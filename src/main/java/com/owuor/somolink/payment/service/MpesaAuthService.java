package com.owuor.somolink.payment.service;

import com.owuor.somolink.payment.config.MpesaDarajaProperties;
import com.owuor.somolink.payment.dto.MpesaAuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaAuthService {

    private final MpesaDarajaProperties config;
    private final RestTemplate restTemplate;

    public String getAccessToken() {

        String credentials = config.getConsumerKey() + ":" + config.getConsumerSecret();
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<MpesaAuthResponse> response = restTemplate.exchange(
                config.getOauthEndpoint() + "?grant_type=" + config.getGrantType(),
                HttpMethod.GET,
                request,
                MpesaAuthResponse.class
        );

        MpesaAuthResponse body = response.getBody();

        if (body == null || body.getAccessToken() == null) {
            log.error("Failed to retrieve M-Pesa access token");
            throw new IllegalStateException("M-Pesa authentication failed");
        }

        return body.getAccessToken();
    }
}
