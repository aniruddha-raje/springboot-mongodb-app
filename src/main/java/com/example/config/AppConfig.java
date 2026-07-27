package com.example.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Infrastructure {@code @Enable*} annotations live here rather than on
 * {@link com.example.DemoApplication} so that they are not pulled into
 * web-layer slice tests (e.g. {@code @WebMvcTest}), which would otherwise
 * try to build the MongoDB repository beans without a Mongo context.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.example.repository")
@EnableCaching
@EnableAsync
@EnableAspectJAutoProxy
public class AppConfig {
}
