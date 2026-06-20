package com.payment_processing_platform.account_service.dto;


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
public class AccountDebitedEvent {
    private UUID paymentId;
    private String sourceBankId;
    private String sourceAccountNumber;
    private String destinationBankId;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
}