package com.example.jwtlogin.redis.util;

import io.micrometer.common.util.StringUtils;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class RedisUtils {

    private final StringRedisTemplate redisTemplate;

    public RedisUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setRedisValueWithTimeout(String key, String value, long ttl) {
        this.redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.MILLISECONDS);
    }

    public String getRedisValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void modifyRedisValue(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String existingValue = valueOperations.get(key);
        if (StringUtils.isNotEmpty(existingValue)) {
            long ttl = redisTemplate.getExpire(key);
            valueOperations.set(key, value, ttl, TimeUnit.SECONDS);
        }
    }

    public void removeRedisValue(String key) {
        redisTemplate.delete(key);
    }

}
