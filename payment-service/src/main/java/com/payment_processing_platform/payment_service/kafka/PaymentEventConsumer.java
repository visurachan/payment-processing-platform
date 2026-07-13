package com.payment_processing_platform.payment_service.kafka;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment_processing_platform.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payments.account.debited",
            groupId = "payment-service-group"
    )
    public void onAccountDebited(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            UUID paymentId = UUID.fromString(event.get("paymentId").toString());

            log.info("Received AccountDebited for payment {}", paymentId);

            paymentService.markCompleted(paymentId);

        } catch (Exception e) {
            log.error("Failed to process AccountDebited: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "payments.account.failed",
            groupId = "payment-service-group"
    )
    public void onPaymentFailed(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            UUID paymentId = UUID.fromString(event.get("paymentId").toString());
            String reason = event.get("reason") != null ?
                    event.get("reason").toString() : "UNKNOWN";

            log.info("Received PaymentFailed for payment {} — reason: {}",
                    paymentId, reason);

            paymentService.markFailed(paymentId, reason);

        } catch (Exception e) {
            log.error("Failed to process PaymentFailed: {}", e.getMessage(), e);
        }
    }
}