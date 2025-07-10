package com.guardianes.testconfig;

import static org.mockito.Mockito.mock;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
@EnableAutoConfiguration(
        exclude = {RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
public class NoRedisTestConfiguration {

    @Bean
    @Primary
    public RedisConnectionFactory mockRedisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }

    @Bean("redisTemplate")
    @Primary
    public RedisTemplate<String, Object> mockRedisTemplate() {
        return mock(RedisTemplate.class);
    }

    @Bean
    @Primary
    public RedisKeyValueAdapter mockRedisKeyValueAdapter() {
        return mock(RedisKeyValueAdapter.class);
    }
}
