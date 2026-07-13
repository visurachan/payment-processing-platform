package com.payment_processing_platform.webhook_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment_processing_platform.webhook_service.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookConsumer {
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payments.completed",
            groupId = "webhook-service-group"
    )
    public void onPaymentComplted(String message){
        try {
            Map<String,Object> event = objectMapper.readValue(message,Map.class);
            String callbackUrl = event.get("merchantCallbackUrl") != null
                    ? event.get("merchantCallbackUrl").toString() : null;

            if (callbackUrl == null) {
                log.warn("No merchantCallbackUrl for payment {} — skipping webhook",
                        event.get("paymentId"));
                return;
            }

            webhookService.send(callbackUrl, Map.of(
                    "paymentId", event.get("paymentId").toString(),
                    "status", "COMPLETED"
            ));

        } catch (Exception e){
            log.error("Failed to handle PaymentCompleted webhook: {}", e.getMessage(), e);
        }
    }
    @KafkaListener(
            topics = "payments.failed",
            groupId = "webhook-service-group")
    public void onPaymentFailed(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String callbackUrl = event.get("merchantCallbackUrl") != null
                    ? event.get("merchantCallbackUrl").toString() : null;

            if (callbackUrl == null) {
                log.warn("No merchantCallbackUrl for payment {} — skipping webhook",
                        event.get("paymentId"));
                return;
            }

            webhookService.send(callbackUrl, Map.of(
                    "paymentId", event.get("paymentId").toString(),
                    "status", "FAILED",
                    "reason", event.get("reason").toString()
            ));
        } catch (Exception e) {
            log.error("Failed to handle PaymentFailed webhook: {}", e.getMessage(), e);
        }
    }



}
