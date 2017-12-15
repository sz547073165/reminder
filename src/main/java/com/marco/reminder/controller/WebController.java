package com.marco.reminder.controller;

import com.alibaba.fastjson.JSONArray;
import com.marco.reminder.model.EmailText;
import com.marco.reminder.util.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

@Controller
public class WebController {
    @Autowired
    private RedisClient redisClient;

    @ResponseBody
    @GetMapping("/emailtext")
    public String getTaskEmailText() {
        ArrayList<String> emailTextKeyList = new ArrayList<>();
        emailTextKeyList.add("task1EmailText");
        emailTextKeyList.add("task2EmailText");
        emailTextKeyList.add("task3EmailText");
        ArrayList<EmailText> emailTextList = new ArrayList<>();
        for (String key : emailTextKeyList) {
            emailTextList.add(redisClient.get(key, EmailText.class));
        }
        return JSONArray.toJSONString(emailTextList);
    }
}
