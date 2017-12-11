package com.marco.reminder.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisClient {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, Object object) {
        String objStr = JSONObject.toJSONString(object);
        set(key, objStr);
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public <T> T get(String key, Class<T> tClass) {
        String value = get(key);
        return JSONObject.parseObject(value, tClass);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }
}
