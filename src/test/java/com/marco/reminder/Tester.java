package com.marco.reminder;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Administrator on 2017/12/7 0007.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class Tester {
    public void print(Object obj) {
        System.out.println(obj);
    }
}
