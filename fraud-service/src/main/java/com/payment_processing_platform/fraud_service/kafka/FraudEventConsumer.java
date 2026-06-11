package com.payment_processing_platform.fraud_service.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment_processing_platform.fraud_service.dto.FraudCheckedEvent;
import com.payment_processing_platform.fraud_service.dto.PaymentInitiatedEvent;
import com.payment_processing_platform.fraud_service.service.FraudRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudEventConsumer {

    private final FraudRuleEngine fraudRuleEngine;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payments.initiated",
            groupId = "fraud-service-group"
    )
    public void onPaymentInitiated(String message) {
        try {
            PaymentInitiatedEvent event = objectMapper
                    .readValue(message, PaymentInitiatedEvent.class);

            log.info("Received PaymentInitiated for payment {}",
                    event.getPaymentId());

            FraudCheckedEvent result = fraudRuleEngine.evaluate(event);

            String payload = objectMapper.writeValueAsString(result);

            kafkaTemplate.send(
                    "payments.fraud.checked",
                    event.getPaymentId().toString(),
                    payload
            );

            log.info("Published FraudChecked — payment {} result: {}",
                    event.getPaymentId(), result.getResult());

        } catch (Exception e) {
            log.error("Failed to process PaymentInitiated message: {}",
                    e.getMessage(), e);
        }
    }
}