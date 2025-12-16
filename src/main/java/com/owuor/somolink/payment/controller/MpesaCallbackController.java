package com.owuor.somolink.payment.controller;

import com.owuor.somolink.payment.dto.MpesaCallbackRequest;
import com.owuor.somolink.payment.service.MpesaCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/validate")
@RequiredArgsConstructor
public class MpesaCallbackController {

    private final MpesaCallbackService callbackService;

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestBody MpesaCallbackRequest request
    ) {
        try {
            log.info("M-Pesa callback received");

            callbackService.handleCallback(request);

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            // VERY IMPORTANT:
            log.error("Callback handling failed", e);
            return ResponseEntity.ok("OK");
        }
    }
}
