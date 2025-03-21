package com.epam.AsyncDataPipeline.config;

import com.epam.AsyncDataPipeline.constants.TaskManagementConstants;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for setting up Caffeine Cache in a Spring Boot application.
 * This class enables caching support and defines a Caffeine-based cache manager
 * with specific configurations, such as expiration policies and statistics tracking.
 */
@Configuration
@EnableCaching
public class CaffeineCacheConfig {


    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(TaskManagementConstants.APP_NAME);
        cacheManager.setCaffeine(caffeineConfig());
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // Expire entries after 5 min
                .recordStats();
    }
}