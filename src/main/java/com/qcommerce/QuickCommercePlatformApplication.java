package com.qcommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Quick Commerce Platform - Main Application Entry Point
 * 
 * Architecture: Hexagonal (Ports & Adapters)
 * 
 * Package Structure:
 * - domain: Core business logic, entities, domain services (Framework-agnostic)
 * - application: Use cases, application services, ports (Orchestration layer)
 * - infrastructure: Adapters for DB, Redis, external APIs (Implementation details)
 * - interfaces: REST Controllers, WebSocket handlers (Delivery mechanisms)
 * 
 * @author Q-Commerce Engineering Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableTransactionManagement
@EnableAsync
public class QuickCommercePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickCommercePlatformApplication.class, args);
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════╗
            ║   Quick Commerce Platform - Production Ready            ║
            ║   Spring Boot 3.2 | Java 21 | MySQL 8.0 | Redis        ║
            ║   Swagger UI: http://localhost:8080/swagger-ui.html     ║
            ╚══════════════════════════════════════════════════════════╝
            """);
    }
}
