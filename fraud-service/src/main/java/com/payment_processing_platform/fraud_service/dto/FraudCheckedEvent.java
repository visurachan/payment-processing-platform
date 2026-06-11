package com.payment_processing_platform.fraud_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudCheckedEvent {
    private UUID paymentId;
    private String result;       // APPROVED or REJECTED
    private String reason;       // null if approved, reason if rejected
}