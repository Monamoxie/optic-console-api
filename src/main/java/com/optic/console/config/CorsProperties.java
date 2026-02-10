package com.optic.console.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "cors")
@Component
@Getter
@Setter
public class CorsProperties {
    private List<String> allowedOrigins = List.of();
}
