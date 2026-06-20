package com.payment_processing_platform.account_service.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class FraudCheckedEvent {
    private UUID paymentId;
    private String result;
    private String reason;
    private String sourceBankId;
    private String sourceAccountNumber;
    private String destinationBankId;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String reference;
}