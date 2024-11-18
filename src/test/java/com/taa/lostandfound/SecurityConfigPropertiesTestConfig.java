package com.taa.lostandfound;

import com.taa.lostandfound.security.SecurityConfigProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SecurityConfigPropertiesTestConfig {

    @Bean
    public SecurityConfigProperties securityConfigProperties() {
        return new SecurityConfigProperties("testSecretKey1234567bhgfj91jdtestSecretKey1234567bhgfj91jd");
    }
}
