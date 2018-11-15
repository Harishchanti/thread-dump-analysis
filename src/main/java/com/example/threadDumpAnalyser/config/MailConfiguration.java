package com.example.threadDumpAnalyser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "threaddumpemail")
@Data
@Component
public class MailConfiguration {

    private String protocol = "smtp";
    private String host = "smtp.gmail.com";
    private int port = 587;
    private boolean auth = true;
    private boolean enable = true;
    private String username;
    private String password;

}