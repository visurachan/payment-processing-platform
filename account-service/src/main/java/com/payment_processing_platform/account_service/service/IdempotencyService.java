package com.payment_processing_platform.account_service.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment_processing_platform.account_service.entity.IdempotencyKey;
import com.payment_processing_platform.account_service.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    public boolean hasBeenProcessed(String key) {
        return idempotencyKeyRepository.existsById(key);
    }

    @Transactional
    public void store(String key, UUID paymentId,
                      String operation, Object result) {
        try {
            IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                    .idempotencyKey(key)
                    .paymentId(paymentId)
                    .operation(operation)
                    .result(objectMapper.writeValueAsString(result))
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                    .build();

            idempotencyKeyRepository.save(idempotencyKey);
        } catch (Exception e) {
            log.error("Failed to store idempotency key {}", key, e);
        }
    }

    // Clean up expired keys every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredKeys() {
        idempotencyKeyRepository.deleteExpired(Instant.now());
        log.debug("Cleaned up expired idempotency keys");
    }
}
