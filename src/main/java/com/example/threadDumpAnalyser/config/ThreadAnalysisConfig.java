package com.example.threadDumpAnalyser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "threadanalysis")
@Data
@Component
public class ThreadAnalysisConfig {
    String[] recipients;
    String serviceName;
    String cronPattern;
}
