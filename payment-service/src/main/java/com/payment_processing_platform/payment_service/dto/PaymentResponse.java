package com.payment_processing_platform.payment_service.dto;

import com.payment_processing_platform.payment_service.entity.Enums.PaymentStatus;
import com.payment_processing_platform.payment_service.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse (

        UUID paymentId,
        String merchantId,
        String sourceBankId,
        String sourceAccountNumber,
        String destinationBankId,
        String destinationAccountNumber,
        BigDecimal amount,
        String currency,
        String reference,
        PaymentStatus paymentStatus,
        String failureReason,
        String merchantCallbackUrl,
        Instant createdAt,
        Instant updatedAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchantId(),
                payment.getSourceBankId(),
                payment.getSourceAccountNumber(),
                payment.getDestinationBankId(),
                payment.getDestinationAccountNumber(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getReference(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.getMerchantCallbackUrl(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}

