package com.marco.reminder.task;

import com.alibaba.fastjson.JSONObject;
import com.marco.reminder.core.MatchSelecter;
import com.marco.reminder.util.EmailMisc;
import com.marco.reminder.util.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class MatchTask {
    @Autowired
    private RedisClient redisClient;

    @Autowired
    private MatchSelecter matchSelecter;

    private String task1EmailStr = "";

    private String task2EmailStr = "";

    /**
     * 更新redis cache里的比赛信息
     *
     * @throws Exception
     */
    @Scheduled(fixedDelay = 10 * 1000)
    public void updateMatchesStrList() throws Exception {
        String matchesStrListInCache = null;
        if (redisClient.hasKey("matchesStrList")) {
            matchesStrListInCache = redisClient.get("matchesStrList");
        }
        ArrayList<String> matchesStrList = matchSelecter.getMatchesStrList();
        if (matchesStrList == null && matchesStrList.size() <= 0) {
            return;
        }
        String temp = JSONObject.toJSONString(matchesStrList);
        if (temp.equals(matchesStrListInCache)) {
            return;
        }
        redisClient.set("matchesStrList", temp);
    }

    /**
     * 比赛：70min，大0.5
     *
     * @throws Exception
     */
    @Scheduled(fixedDelay = 1 * 30 * 1000)
    public void task1() throws Exception {
        ArrayList<HashMap<String, String>> matchIdList = matchSelecter.get70minMatch();
        print(String.format("%s 70min match：%s", new Date(), matchIdList));
        String text = "";
        for (Map<String, String> temp : matchIdList) {
            ArrayList<Integer> noGoalList = matchSelecter.getNoGoalCount(temp.get("matchId"));
            print(noGoalList);
            if (noGoalList != null && (noGoalList.get(0) >= 2 || noGoalList.get(1) >= 2)) {
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

    /**
     * 比赛：未来三小时内，可能70min，大0.5
     *
     * @throws Exception
     */
    @Scheduled(fixedDelay = 3 * 60 * 60 * 1000)
    public void task2() throws Exception {
        print(String.format("%s 00min match", new Date()));
        ArrayList<HashMap<String, String>> matchIdList = matchSelecter.get00minMatch();
        String text = "";
        for (Map<String, String> temp : matchIdList) {
            ArrayList<Integer> noGoalList = matchSelecter.getNoGoalCount(temp.get("matchId"));
            if (noGoalList != null && (noGoalList.get(0) >= 2 || noGoalList.get(1) >= 2)) {
                text = String.format("%s<p>%s</p><p>%s</p><p></p>", text, JSONObject.toJSONString(temp), JSONObject.toJSONString(noGoalList));
            } else {
                continue;
            }
        }
        if (!"".equals(text) && !task2EmailStr.equals(text)) {
            EmailMisc.sendEmail("00min match", text);
            task2EmailStr = text;
        }
    }

    private void print(Object object) {
        System.out.println(object);
    }
}
