package com.guardianes.testconfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class SecurityTestConfiguration {

    @Bean
    @Primary
    public RedisConnectionFactory mockRedisConnectionFactory() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        RedisStringCommands stringCommands = mock(RedisStringCommands.class);
        RedisKeyCommands keyCommands = mock(RedisKeyCommands.class);
        RedisServerCommands serverCommands = mock(RedisServerCommands.class);

        // Mock the connection and its commands
        when(factory.getConnection()).thenReturn(connection);
        when(connection.stringCommands()).thenReturn(stringCommands);
        when(connection.keyCommands()).thenReturn(keyCommands);
        when(connection.serverCommands()).thenReturn(serverCommands);

        // Mock string operations
        when(stringCommands.get(any(byte[].class))).thenReturn(null);
        when(stringCommands.set(any(byte[].class), any(byte[].class))).thenReturn(true);
        when(stringCommands.setEx(any(byte[].class), any(long.class), any(byte[].class)))
                .thenReturn(true);

        // Mock key operations
        when(keyCommands.exists(any(byte[].class))).thenReturn(false);
        when(keyCommands.del(any(byte[].class))).thenReturn(1L);
        when(keyCommands.expire(any(byte[].class), any(long.class))).thenReturn(true);

        // Mock server operations (for health check)
        Properties redisInfo = new Properties();
        redisInfo.setProperty("redis_version", "7.0.0");
        redisInfo.setProperty("redis_mode", "standalone");
        when(serverCommands.info()).thenReturn(redisInfo);
        when(serverCommands.info(any(String.class))).thenReturn(redisInfo);

        return factory;
    }

    @Bean("redisTemplate")
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> mockRedisTemplate() {
        RedisTemplate<String, Object> template = mock(RedisTemplate.class);

        // Mock common operations
        when(template.hasKey(anyString())).thenReturn(false);
        when(template.opsForValue())
                .thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        when(template.opsForHash())
                .thenReturn(mock(org.springframework.data.redis.core.HashOperations.class));

        return template;
    }

    @Bean
    @Primary
    public RedisKeyValueAdapter mockRedisKeyValueAdapter() {
        return mock(RedisKeyValueAdapter.class);
    }

    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public HealthIndicator testHealthIndicator() {
        return () ->
                Health.up().withDetail("status", "UP").withDetail("test", "security-test").build();
    }
}
