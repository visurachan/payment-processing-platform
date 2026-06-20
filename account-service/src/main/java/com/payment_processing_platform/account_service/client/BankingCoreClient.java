package com.payment_processing_platform.account_service.client;

import com.payment_processing_platform.account_service.config.BankingCoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
public class BankingCoreClient {

    private final RestClient restClient;
    private final BankingCoreProperties bankingCoreProperties;

    public BankingCoreClient(
            BankingCoreProperties bankingCoreProperties) {
        this.restClient = RestClient.create();
        this.bankingCoreProperties = bankingCoreProperties;
    }

    public void debit(String bankId, String accountNumber,
                      BigDecimal amount, String currency,
                      String idempotencyKey, String paymentReference) {
        String url = getBankUrl(bankId);
        log.info("Debiting account {} at bank {} — amount: {} {}",
                accountNumber, bankId, amount, currency);

        restClient.post()
                .uri(url + "/api/v1/processor/accounts/{accountNumber}/debit",
                        accountNumber)
                .header("Idempotency-Key", idempotencyKey)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "amount", amount,
                        "currency", currency,
                        "paymentReference", paymentReference
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public void credit(String bankId, String accountNumber,
                       BigDecimal amount, String currency,
                       String idempotencyKey, String paymentReference) {
        String url = getBankUrl(bankId);
        log.info("Crediting account {} at bank {} — amount: {} {}",
                accountNumber, bankId, amount, currency);

        restClient.post()
                .uri(url + "/api/v1/processor/accounts/{accountNumber}/credit",
                        accountNumber)
                .header("Idempotency-Key", idempotencyKey)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "amount", amount,
                        "currency", currency,
                        "paymentReference", paymentReference
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public void reverseDebit(String bankId, String accountNumber,
                             BigDecimal amount, String originalIdempotencyKey,
                             String paymentReference) {
        String url = getBankUrl(bankId);
        log.warn("Reversing debit on account {} at bank {} — amount: {} {}",
                accountNumber, bankId, amount, paymentReference);

        restClient.post()
                .uri(url + "/api/v1/processor/accounts/{accountNumber}/debit/reverse",
                        accountNumber)
                .header("Idempotency-Key", "reverse:" + originalIdempotencyKey)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "originalIdempotencyKey", originalIdempotencyKey,
                        "amount", amount,
                        "paymentReference", paymentReference
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public boolean accountExists(String bankId, String accountNumber) {
        String url = getBankUrl(bankId);
        try {
            restClient.get()
                    .uri(url + "/api/v1/processor/accounts/{accountNumber}/exists",
                            accountNumber)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getBankUrl(String bankId) {
        String url = bankingCoreProperties.getBanks().get(bankId);
        if (url == null) {
            throw new IllegalArgumentException("Unknown bank: " + bankId);
        }
        return url;
    }
}
