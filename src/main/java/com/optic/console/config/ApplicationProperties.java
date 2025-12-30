package com.optic.console.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "application")
@Component
@Getter
@Setter
public class ApplicationProperties {
    private String url;
    private String name;
}