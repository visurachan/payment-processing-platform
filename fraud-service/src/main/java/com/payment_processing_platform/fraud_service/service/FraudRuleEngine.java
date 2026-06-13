package com.payment_processing_platform.fraud_service.service;



import com.payment_processing_platform.fraud_service.dto.FraudCheckedEvent;
import com.payment_processing_platform.fraud_service.dto.PaymentInitiatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class FraudRuleEngine {

    public FraudCheckedEvent evaluate(PaymentInitiatedEvent event) {

        log.info("Evaluating fraud rules for payment {} — amount: {} {}",
                event.getPaymentId(), event.getAmount(), event.getCurrency());

        // Rule 1 — Self payment detection
        // Only the payment platform can catch this —
        // the bank only sees one side of the transaction
        if (isSelfPayment(event)) {
            log.warn("FRAUD DETECTED — self payment on payment {}",
                    event.getPaymentId());
            return  FraudCheckedEvent.builder()
                    .paymentId(event.getPaymentId())
                    .result("REJECTED")
                    .reason("SELF_PAYMENT_DETECTED")
                    .build();

        }

        // Rule 2 — Large cross-bank amount
        // Cross-bank transfers above £5000 are flagged as high risk
        // Only the payment platform can see both sides of the transaction
        if (isCrossBankLargeAmount(event)) {
            log.warn("FRAUD DETECTED — large cross-bank amount {} {} on payment {}",
                    event.getAmount(), event.getCurrency(), event.getPaymentId());
            return FraudCheckedEvent.builder()
                    .paymentId(event.getPaymentId())
                    .result("REJECTED")
                    .reason("CROSS_BANK_LARGE_AMOUNT_EXCEEDED")
                    .build();

        }

        // Rule 3 — Null or zero amount
        // Defensive check should be caught by validation
        // but fraud engine is last line of defence
        if (event.getAmount() == null ||
                event.getAmount().signum() <= 0) {
            log.warn("FRAUD DETECTED — invalid amount on payment {}",
                    event.getPaymentId());
            return FraudCheckedEvent.builder()
                    .paymentId(event.getPaymentId())
                    .result("REJECTED")
                    .reason("INVALID_AMOUNT")
                    .build();

        }

        // All rules passed — approved
        log.info("Fraud check APPROVED for payment {}", event.getPaymentId());
        return FraudCheckedEvent.builder()
                .paymentId(event.getPaymentId())
                .result("APPROVED")
                .reason(null)
                .build();

    }

    private boolean isSelfPayment(PaymentInitiatedEvent event) {
        return event.getSourceBankId().equals(event.getDestinationBankId()) &&
                event.getSourceAccountNumber()
                        .equals(event.getDestinationAccountNumber());
    }
    private boolean isCrossBankLargeAmount(PaymentInitiatedEvent event) {
        BigDecimal threshold = new BigDecimal("5000");
        return !event.getSourceBankId().equals(event.getDestinationBankId()) &&
                event.getAmount().compareTo(threshold) > 0;
    }
}