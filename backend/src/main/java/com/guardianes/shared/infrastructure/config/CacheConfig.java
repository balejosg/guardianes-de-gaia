package com.guardianes.shared.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis cache configuration for the Guardianes application. Provides optimized caching for
 * frequently accessed game data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Configures Redis cache manager with optimized settings for game data */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configure ObjectMapper for proper serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30)) // Default TTL: 30 minutes
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        serializer))
                        .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // Specific cache configurations
                .withCacheConfiguration(
                        "daily-aggregates",
                        defaultConfig.entryTtl(Duration.ofHours(2))) // Longer TTL for daily data
                .withCacheConfiguration(
                        "energy-balance",
                        defaultConfig.entryTtl(
                                Duration.ofMinutes(15))) // Shorter TTL for frequently changing data
                .withCacheConfiguration(
                        "step-history",
                        defaultConfig.entryTtl(
                                Duration.ofHours(1))) // Medium TTL for historical data
                .withCacheConfiguration(
                        "energy-transactions",
                        defaultConfig.entryTtl(
                                Duration.ofMinutes(45))) // Medium TTL for transaction history
                .withCacheConfiguration(
                        "guardian-profile",
                        defaultConfig.entryTtl(Duration.ofHours(6))) // Long TTL for profile data
                .build();
    }

    /** Custom cache key generator for complex objects */
    @Bean
    public org.springframework.cache.interceptor.KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(target.getClass().getSimpleName()).append(":");
            keyBuilder.append(method.getName()).append(":");

            for (Object param : params) {
                if (param != null) {
                    keyBuilder.append(param.toString()).append(":");
                }
            }

            // Remove trailing colon
            if (keyBuilder.length() > 0 && keyBuilder.charAt(keyBuilder.length() - 1) == ':') {
                keyBuilder.setLength(keyBuilder.length() - 1);
            }

            return keyBuilder.toString();
        };
    }
}
