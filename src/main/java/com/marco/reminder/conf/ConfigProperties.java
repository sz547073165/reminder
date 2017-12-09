package com.marco.reminder.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by Administrator on 2017/12/7 0007.
 */
@Configuration
@PropertySource(value = "classpath:application.properties", encoding = "utf-8")
public class ConfigProperties {
    @Value("${spring.profiles.active}")
    private String active;

    @Value("${com.marco.reminder.name}")
    private String name;

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
