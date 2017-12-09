package com.marco.reminder.conf;

import com.marco.reminder.Tester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/12/7 0007.
 */
public class ConfigPropertiesTest extends Tester{
    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private ProfilesProperties profilesProperties;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getValueTest() throws Exception {
        print(profilesProperties.getPort());
        print(configProperties.getActive());
        print(configProperties.getName());
    }

}