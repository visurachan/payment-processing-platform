package com.payment_processing_platform.payment_service.controller;



import com.payment_processing_platform.payment_service.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/internal/merchants")
@RequiredArgsConstructor
public class MerchantValidationController {

    private final MerchantRepository merchantRepository;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validate(
            @RequestHeader("X-API-Key") String apiKey) {

        return merchantRepository.findByApiKey(apiKey)
                .filter(m -> m.getActive())
                .map(m -> ResponseEntity.ok(Map.of("merchantId", m.getId())))
                .orElse(ResponseEntity.status(401).build());
    }
}