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

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

}
