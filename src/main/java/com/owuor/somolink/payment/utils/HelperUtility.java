package com.owuor.somolink.payment.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Slf4j
public class HelperUtility {

    /**
     * Convert string to Base64
     */
    public static String toBase64String(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            log.error("JSON conversion error", exception);
            return null;
        }
    }

    /**
     * Generate encrypted security credentials using a certificate
     */
    @SneakyThrows
    public static String getSecurityCredentials(String initiatorPassword) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            byte[] input = initiatorPassword.getBytes(StandardCharsets.UTF_8);

            Resource resource = new ClassPathResource("cert.cer");
            InputStream inputStream = resource.getInputStream();

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);

            PublicKey publicKey = certificate.getPublicKey();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] cipherText = cipher.doFinal(input);

            return Base64.getEncoder().encodeToString(cipherText).trim();

        } catch (NoSuchAlgorithmException | CertificateException | InvalidKeyException |
                 NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 NoSuchProviderException | IOException e) {

            log.error("Error generating security credentials", e);
            throw e;
        }
    }

    /**
     * Generate a unique 12-character transaction number
     */
    public static String getTransactionUniqueNumber() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'Z')
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .build();

        String transactionNumber = generator.generate(12);
        log.info("Transaction Number: {}", transactionNumber);
        return transactionNumber;
    }

    /**
     * Generate STK Push password
     */
    public static String getStkPushPassword(String shortCode, String passKey, String timestamp) {
        String data = shortCode + passKey + timestamp;
        return toBase64String(data);
    }

    /**
     * Get current timestamp in yyyyMMddHHmmss
     */
    public static String getTransactionTimestamp() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }
}
