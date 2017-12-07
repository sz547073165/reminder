package com.marco.reminder.task;

import com.alibaba.fastjson.JSONObject;
import com.marco.reminder.core.MatchSelecter;
import com.marco.reminder.util.EmailMisc;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class MatchTask {
    private String task1EmailStr = "";

    @Scheduled(fixedDelay = 1 * 60 * 1000)
    public void task1() throws Exception {
        System.out.println(new Date());
        ArrayList<HashMap<String, String>> matchIdList = MatchSelecter.get70minMatchId();
        System.out.println(String.format("70min match：%s", matchIdList));
        String text = "";
        for (Map<String, String> temp : matchIdList) {
            ArrayList<Integer> noGoalList = MatchSelecter.getNoGoalCount(temp.get("matchId"));
            System.out.println(noGoalList);
            if (noGoalList.get(0) >= 3 || noGoalList.get(1) >= 3) {
                text = String.format("%s<p>%s</p><p>%s</p><p></p>", text, JSONObject.toJSONString(temp), JSONObject.toJSONString(noGoalList));
            } else {
                continue;
            }
        }
        if (!"".equals(text) && !task1EmailStr.equals(text)) {
            EmailMisc.sendEmail("70min match", text);
            task1EmailStr = text;
        }
    }
}
