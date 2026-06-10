package com.payment_processing_platform.payment_service.entity.Enums;



public enum PaymentStatus {
    PENDING,
    PROCESSING,
    FRAUD_APPROVED,
    COMPLETED,
    FAILED,
    COMPENSATING
}