package com.taa.lostandfound.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityConfigProperties(
        String secret
) { }
