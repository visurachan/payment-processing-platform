package com.payment_processing_platform.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment_processing_platform.payment_service.dto.InitiatePaymentRequest;
import com.payment_processing_platform.payment_service.dto.PaymentResponse;
import com.payment_processing_platform.payment_service.entity.Enums.PaymentStatus;
import com.payment_processing_platform.payment_service.entity.OutboxEvent;
import com.payment_processing_platform.payment_service.entity.Payment;
import com.payment_processing_platform.payment_service.repository.OutboxRepository;
import com.payment_processing_platform.payment_service.repository.PaymentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PaymentResponse initiatePayment(String idempotencyKey, String merchantId, @Valid InitiatePaymentRequest request) {

        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing ->{
                    log.info("Duplicate request detected - returning existing payment {}",
                            existing.getId());
                    return PaymentResponse.from(existing);

                })
                .orElseGet(() -> {
                    Payment payment = Payment.builder()
                            .idempotencyKey(idempotencyKey)
                            .merchantId(merchantId)
                            .sourceBankId(request.sourceBankId())
                            .sourceAccountNumber(request.sourceAccountNumber())
                            .destinationBankId(request.destinationBankId())
                            .destinationAccountNumber(request.destinationAccountNumber())
                            .amount(request.amount())
                            .currency(request.currency())
                            .reference(request.reference())
                            .merchantCallbackUrl(request.merchantCallbackUrl())
                            .status(PaymentStatus.PENDING)
                            .build();

                    Payment saved = paymentRepository.save(payment);

                    log.info("Payment created - id: {}, merchant: {}, amount: {} {}",
                            payment.getId(), merchantId,
                            request.amount(), request.currency());
                    try {
                        String payLoad = objectMapper.writeValueAsString(
                                buildPaymentInitiatedPayload(saved));
                        OutboxEvent outboxEvent = OutboxEvent.builder()
                                .aggregateId(saved.getId())
                                .eventType("PaymentInitiated")
                                .payload(payLoad)
                                .build();

                        outboxRepository.save(outboxEvent);

                        log.info("Payment {} created with outbox event — merchant: {}",
                                saved.getId(), merchantId);
                    } catch (Exception e) {
                        log.error("Failed to create outbox event for payment {}",
                                saved.getId(), e);
                        throw new RuntimeException("Failed to create payment event", e);

                    }


                    return PaymentResponse.from(saved);
                });
    }
    private PaymentInitiatedPayload buildPaymentInitiatedPayload(Payment payment) {
        return new PaymentInitiatedPayload(
                payment.getId(),
                payment.getMerchantId(),
                payment.getSourceBankId(),
                payment.getSourceAccountNumber(),
                payment.getDestinationBankId(),
                payment.getDestinationAccountNumber(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getReference()
        );
    }

    public PaymentResponse getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found: "+ paymentId));


    }

    // Inner record — the payload that goes into the outbox and then to Kafka
    public record PaymentInitiatedPayload(
            UUID paymentId,
            String merchantId,
            String sourceBankId,
            String sourceAccountNumber,
            String destinationBankId,
            String destinationAccountNumber,
            java.math.BigDecimal amount,
            String currency,
            String reference
    ) {}
}
