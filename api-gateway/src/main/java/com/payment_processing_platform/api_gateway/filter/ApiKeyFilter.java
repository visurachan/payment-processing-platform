package com.payment_processing_platform.api_gateway.filter;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;

    public ApiKeyFilter(WebClient.Builder webClientBuilder,
                        @Value("${payment.service.url}") String paymentServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(paymentServiceUrl)
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();

        // Skip actuator — no auth needed
        if (path.contains("/actuator")) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest()
                .getHeaders()
                .getFirst("X-API-Key");

        // No API key provided
        if (apiKey == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Call payment service to validate the key
        return webClient.get()
                .uri("/internal/merchants/validate")
                .header("X-API-Key", apiKey)
                .retrieve()
                .bodyToMono(MerchantValidationResponse.class)
                .flatMap(response -> {
                    // Valid key — stamp merchantId and forward
                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r.header("X-Merchant-ID", response.merchantId()))
                            .build();
                    return chain.filter(mutated);
                })
                .onErrorResume(ex -> {
                    // Invalid key or payment service returned 401
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() { return 0; }


    record MerchantValidationResponse(String merchantId) {}
}