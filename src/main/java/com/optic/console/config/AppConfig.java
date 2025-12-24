package com.optic.console.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${spring.application.url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public String baseUrl() {
        return baseUrl;
    }
}