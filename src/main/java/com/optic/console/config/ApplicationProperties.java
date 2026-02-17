package com.optic.console.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "application")
@Component
@Getter
@Setter
public class ApplicationProperties {
    private String url;
    private String name;
    private String frontendUrl;
}