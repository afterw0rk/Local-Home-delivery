package com.qcommerce.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Retry Configuration
 * Enables @Retryable annotation for handling optimistic locking failures
 */
@Configuration
@EnableRetry
public class RetryConfig {
    // Spring Retry is now enabled globally
    // Services can use @Retryable annotation for automatic retry logic
}
