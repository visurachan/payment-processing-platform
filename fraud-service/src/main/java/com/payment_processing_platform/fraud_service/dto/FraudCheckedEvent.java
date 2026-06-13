package com.payment_processing_platform.fraud_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudCheckedEvent {
    private UUID paymentId;
    private String result;       // APPROVED or REJECTED
    private String reason;       // null if approved, reason if rejected

    // Full payment context — passed through from PaymentInitiated
    private String sourceBankId;
    private String sourceAccountNumber;
    private String destinationBankId;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String reference;
}