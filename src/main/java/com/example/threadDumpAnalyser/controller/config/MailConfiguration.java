package com.example.threadDumpAnalyser.controller.config;

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
    private String from = "harish.chanti94@gmail.com";
    private String username = "harish.chanti94@gmail.com";
    private String password = "7411489557";

}