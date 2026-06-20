package com.payment_processing_platform.account_service.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "banking")
@Getter
@Setter
public class BankingCoreProperties {
    private Map<String, String> banks;
}