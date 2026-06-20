package com.payment_processing_platform.account_service.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment_processing_platform.account_service.dto.FraudCheckedEvent;
import com.payment_processing_platform.account_service.service.AccountCoordinatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudCheckedConsumer {

    private final AccountCoordinatorService accountCoordinatorService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payments.fraud.checked",
            groupId = "account-service-group"
    )
    public void onFraudChecked(String message) {
        try {
            FraudCheckedEvent event = objectMapper
                    .readValue(message, FraudCheckedEvent.class);

            log.info("Received FraudChecked for payment {} — result: {}",
                    event.getPaymentId(), event.getResult());

            if ("APPROVED".equals(event.getResult())) {
                accountCoordinatorService.processApprovedPayment(event);
            } else {
                log.info("Payment {} fraud rejected — reason: {}",
                        event.getPaymentId(), event.getReason());
                accountCoordinatorService.handleFraudRejection(
                        event.getPaymentId(), event.getReason());

            }

        } catch (Exception e) {
            log.error("Failed to process FraudChecked message: {}",
                    e.getMessage(), e);
        }
    }
}