package com.payment_processing_platform.account_service.service;


import com.payment_processing_platform.account_service.client.BankingCoreClient;
import com.payment_processing_platform.account_service.dto.AccountDebitedEvent;
import com.payment_processing_platform.account_service.dto.FraudCheckedEvent;
import com.payment_processing_platform.account_service.dto.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountCoordinatorService {

    private final BankingCoreClient bankingCoreClient;
    private final IdempotencyService idempotencyService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void processApprovedPayment(FraudCheckedEvent event) {
        String paymentId = event.getPaymentId().toString();
        String debitKey = "debit:" + paymentId;

        try {
            // 1. Check idempotency — was this already processed?
            if (idempotencyService.hasBeenProcessed(debitKey)) {
                log.info("Payment {} already processed — skipping", paymentId);
                return;
            }

            // 2. Check source account exists
            if (!bankingCoreClient.accountExists(
                    event.getSourceBankId(), event.getSourceAccountNumber())) {
                log.error("Source account {} not found at bank {}",
                        event.getSourceAccountNumber(), event.getSourceBankId());
                publishPaymentFailed(event.getPaymentId().toString(),
                        "SOURCE_ACCOUNT_NOT_FOUND");
                return;
            }

            // 3. Check destination account exists
            if (!bankingCoreClient.accountExists(
                    event.getDestinationBankId(), event.getDestinationAccountNumber())) {
                log.error("Destination account {} not found at bank {}",
                        event.getDestinationAccountNumber(), event.getDestinationBankId());
                publishPaymentFailed(event.getPaymentId().toString(),
                        "DESTINATION_ACCOUNT_NOT_FOUND");
                return;
            }

            // 4. Debit source account
            log.info("Debiting source account {} at {}",
                    event.getSourceAccountNumber(), event.getSourceBankId());

            bankingCoreClient.debit(
                    event.getSourceBankId(),
                    event.getSourceAccountNumber(),
                    event.getAmount(),
                    event.getCurrency(),
                    debitKey,
                    paymentId
            );

            // Store idempotency key after successful debit
            idempotencyService.store(debitKey, event.getPaymentId(),
                    "DEBIT", "SUCCESS");

            // 5. Credit destination account
            log.info("Crediting destination account {} at {}",
                    event.getDestinationAccountNumber(), event.getDestinationBankId());

            String creditKey = "credit:" + paymentId;
            bankingCoreClient.credit(
                    event.getDestinationBankId(),
                    event.getDestinationAccountNumber(),
                    event.getAmount(),
                    event.getCurrency(),
                    creditKey,
                    paymentId
            );

            // 6. Publish AccountDebited event
            AccountDebitedEvent accountDebitedEvent = AccountDebitedEvent.builder()
                    .paymentId(event.getPaymentId())
                    .sourceBankId(event.getSourceBankId())
                    .sourceAccountNumber(event.getSourceAccountNumber())
                    .destinationBankId(event.getDestinationBankId())
                    .destinationAccountNumber(event.getDestinationAccountNumber())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .build();

            String payload = objectMapper.writeValueAsString(accountDebitedEvent);
            kafkaTemplate.send("payments.account.debited",
                    paymentId, payload);

            log.info("Payment {} processed successfully — AccountDebited published",
                    paymentId);

        } catch (org.springframework.web.client.ResourceAccessException e) {
            if (idempotencyService.hasBeenProcessed(debitKey)) {
                log.error("Bank unreachable after debit for payment {} — compensating", paymentId, e);
                compensate(event);
            } else {
                log.error("Bank unreachable before debit for payment {} — failing without compensation", paymentId, e);
                publishPaymentFailed(paymentId, "BANK_UNAVAILABLE");
            }
        } catch (Exception e) {
            log.error("Failed to process payment {} — attempting compensation",
                    paymentId, e);
            compensate(event);
        }
    }

    private void compensate(FraudCheckedEvent event) {
        String paymentId = event.getPaymentId().toString();
        String debitKey = "debit:" + paymentId;

        try {
            // Only reverse if debit actually happened
            if (idempotencyService.hasBeenProcessed(debitKey)) {
                log.warn("Reversing debit for payment {}", paymentId);
                bankingCoreClient.reverseDebit(
                        event.getSourceBankId(),
                        event.getSourceAccountNumber(),
                        event.getAmount(),
                        debitKey,
                        paymentId
                );
                log.info("Debit reversed successfully for payment {}", paymentId);
            }
        } catch (Exception e) {
            log.error("CRITICAL — debit reversal FAILED for payment {} " +
                    "— manual intervention required", paymentId, e);
        } finally {
            publishPaymentFailed(paymentId, "PROCESSING_ERROR");
        }
    }

    private void publishPaymentFailed(String paymentId, String reason) {
        try {
            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .paymentId(java.util.UUID.fromString(paymentId))
                    .reason(reason)
                    .build();

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("payments.account.failed", paymentId, payload);

            log.info("PaymentFailed published for payment {} — reason: {}",
                    paymentId, reason);
        } catch (Exception e) {
            log.error("Failed to publish PaymentFailed for payment {}", paymentId, e);
        }
    }

    public void handleFraudRejection(UUID paymentId, String reason) {
        publishPaymentFailed(paymentId.toString(), "FRAUD_REJECTED_" + reason);
    }
}