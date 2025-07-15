package com.rinha.backend.rinhabackend2025.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ScheduledConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2); // Ajuste conforme testes, mas mantenha baixo
        scheduler.setThreadNamePrefix("outbox-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}
