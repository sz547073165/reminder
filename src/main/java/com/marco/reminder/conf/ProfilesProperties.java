package com.marco.reminder.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by Administrator on 2017/12/7 0007.
 */
@Configuration
//@ConfigurationProperties(prefix = "server")//与@Value("${server.port}")任选其一
@PropertySource(value = "classpath:application-${spring.profiles.active}.properties", encoding = "utf-8")
public class ProfilesProperties {
    @Value("${server.port}")
    private String port;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
