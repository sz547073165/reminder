package com.marco.reminder.util;

import com.marco.reminder.Tester;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisClientTest extends Tester {
    @Autowired
    private RedisClient redisClient;

    @Test
    public void test1() {
        redisClient.set("kkkkkk", "vvvvvv");
        System.out.println(redisClient.get("matchesStrList"));
    }
}