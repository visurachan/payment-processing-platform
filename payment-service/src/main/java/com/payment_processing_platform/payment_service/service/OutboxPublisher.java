package com.payment_processing_platform.payment_service.service;

import com.payment_processing_platform.payment_service.entity.OutboxEvent;
import com.payment_processing_platform.payment_service.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.ScriptAssert;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository
                .findTop10ByStatusOrderByCreatedAtAsc("PENDING");

        if (pending.isEmpty()) return;

        log.info("Outbox poller running — found {} pending events", pending.size());

        for (OutboxEvent event : pending) {
            try {
                String topic = resolveTopic(event.getEventType());
                kafkaTemplate.send(topic,
                                event.getAggregateId().toString(),
                                event.getPayload())
                        .get(5, TimeUnit.SECONDS);

                event.setStatus("PUBLISHED");
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);

                log.info("Published outbox event {} type {} to topic {}",
                        event.getId(), event.getEventType(), topic);

            }  catch (Exception e) {
            log.error("Failed to publish outbox event {} — error: {}",
                    event.getId(), e.getMessage(), e);
        }


        }


    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "PaymentInitiated" -> "payments.initiated";
            case "PaymentCompleted" -> "payments.completed";
            case "PaymentFailed" -> "payments.failed";
            default -> throw new IllegalArgumentException(
                    "Unknown event type: " + eventType);
        };
    }
}
