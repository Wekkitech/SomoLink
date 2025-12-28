package com.owuor.somolink.payment.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mpesa.daraja")
public class MpesaDarajaProperties {

    // OAuth
    private String consumerKey;
    private String consumerSecret;
    private String grantType;
    private String oauthEndpoint;

    // C2B
    private String registerUrlEndpoint;
    private String simulateTransactionEndpoint;
    private String shortCode;
    private String confirmationURL;
    private String validationURL;
    private String responseType;

    // STK Push
    private String stkPassKey;
    private String stkPushShortCode;
    private String stkPushRequestUrl;
    private String stkPushRequestCallbackUrl;
}
