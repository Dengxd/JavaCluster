package com.example;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configure {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // use "rediss://" for SSL connection
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");

        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
