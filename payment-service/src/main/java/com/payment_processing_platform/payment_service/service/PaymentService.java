package com.payment_processing_platform.payment_service.service;

import com.payment_processing_platform.payment_service.dto.InitiatePaymentRequest;
import com.payment_processing_platform.payment_service.dto.PaymentResponse;
import com.payment_processing_platform.payment_service.entity.Enums.PaymentStatus;
import com.payment_processing_platform.payment_service.entity.Payment;
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

                    return PaymentResponse.from(saved);
                });
    }

    public PaymentResponse getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found: "+ paymentId));


    }
}
