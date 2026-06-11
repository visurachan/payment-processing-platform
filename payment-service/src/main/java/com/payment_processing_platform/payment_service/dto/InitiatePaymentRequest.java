package com.payment_processing_platform.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InitiatePaymentRequest (

        @NotBlank(message = "Source bank ID is required")
        String sourceBankId,

        @NotBlank(message = "Source account ID is required")
        String sourceAccountNumber,

        @NotBlank(message = "Destination bank ID is required")
        String destinationBankId,

        @NotBlank(message = "Destination account number is required")
        String destinationAccountNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount should be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Currency type is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code e.g. GBP")
        String currency,

        String reference,

        String merchantCallbackUrl



)


{}
