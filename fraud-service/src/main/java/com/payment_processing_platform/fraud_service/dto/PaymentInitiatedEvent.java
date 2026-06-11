package com.payment_processing_platform.fraud_service.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PaymentInitiatedEvent {
    private UUID paymentId;
    private String merchantId;
    private String sourceBankId;
    private String sourceAccountNumber;
    private String destinationBankId;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String reference;
}