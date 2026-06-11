package com.payment_processing_platform.payment_service.controller;

import com.payment_processing_platform.payment_service.dto.InitiatePaymentRequest;
import com.payment_processing_platform.payment_service.dto.PaymentResponse;
import com.payment_processing_platform.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/V1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-Merchant-ID") String merchantId,
            @Valid @RequestBody InitiatePaymentRequest request) {
        log.info("Payment request received - merchant: {}, amount: {} {}",
                merchantId, request.amount(), request.currency());

        PaymentResponse response = paymentService
                .initiatePayment(idempotencyKey, merchantId, request);
        return ResponseEntity.accepted().body(response);

    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId){
        return ResponseEntity.ok(paymentService.getPayment(paymentId));

    }

}


