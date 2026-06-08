package com.garethyoo.healthchecker;

import com.garethyoo.healthchecker.config.MonitoringProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MonitoringProperties.class)
public class HealthCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthCheckerApplication.class, args);
    }
}
