package com.example.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
@Component
public class Config {

    private String jsonPlaceholderUrl;

    private String baseUrl;

    private String path;
}
