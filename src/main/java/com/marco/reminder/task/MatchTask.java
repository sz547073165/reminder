package com.marco.reminder.task;

import com.alibaba.fastjson.JSONObject;
import com.marco.reminder.core.MatchSelecter;
import com.marco.reminder.model.EmailText;
import com.marco.reminder.util.EmailMisc;
import com.marco.reminder.util.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MatchTask {
    protected static ExecutorService executorService = Executors.newCachedThreadPool();
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MatchSelecter matchSelecter;

    /**
     * 更新redis cache里的比赛信息
     *
     * @throws Exception
     */
    @Scheduled(fixedDelay = 10 * 1000)
    public void updateMatchesStrList() {
        try {
            String matchesStrListInCache = null;
            if (redisClient.hasKey("matchesStrList")) {
                matchesStrListInCache = redisClient.get("matchesStrList");
            }
            ArrayList<String> matchesStrList = null;
            try {
                matchesStrList = matchSelecter.getMatchesStrList();
            } catch (Exception e) {
                return;
            }
            if (matchesStrList == null && matchesStrList.size() <= 0) {
                return;
            }
            String temp = JSONObject.toJSONString(matchesStrList);
            if (temp.equals(matchesStrListInCache)) {
                return;
            }
            redisClient.set("matchesStrList", temp);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * 比赛：70min，大0.5
     *
     * @throws Exception
     */
    @Scheduled(fixedDelay = 1 * 30 * 1000)
    public void task2() {
        try {
            ArrayList<HashMap<String, String>> matchIdList = matchSelecter.get70minMatch();
            print(String.format("%s 70min match：%s", new Date(), matchIdList));
            String key = "task2EmailText";
            String head = "<p><h2>70min，大0.5</h2></p>";
            String text = head;
            for (Map<String, String> temp : matchIdList) {
                ArrayList<Integer> noGoalList = null;
                noGoalList = matchSelecter.getSecondNoGoalCount(temp.get("matchId"));
                print(noGoalList);
                if (noGoalList != null && (noGoalList.get(0) + noGoalList.get(1) >= 3)) {
                    //                if (noGoalList != null && (noGoalList.get(0) >= 2 || noGoalList.get(1) >= 2)) {
                    text = String.format("%s<p>%s</p><p>%s</p><p></p>", text, JSONObject.toJSONString(temp), JSONObject.toJSONString(noGoalList));
                } else {
                    continue;
                }
            }
            EmailText emailText = redisClient.get(key, EmailText.class);
            if (emailText == null) {
                emailText = new EmailText();
                emailText.setKey(key);
            }
            if (!head.equals(text) && !text.equals(emailText.getText())) {
                emailText.setStatus(0);
                emailText.setText(text);
            }
            redisClient.set(key, emailText);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * 比赛：25min，大0.5
     *
     * @throws Exception
     */
    @Scheduled(fixedDelay = 1 * 30 * 1000)
    public void task3() {
        try {
            ArrayList<HashMap<String, String>> matchIdList = matchSelecter.get25minMatch();
            print(String.format("%s 25min match：%s", new Date(), matchIdList));
            String key = "task3EmailText";
            String head = "<p><h2>25min，大0.5</h2></p>";
            String text = head;
            for (Map<String, String> temp : matchIdList) {
                ArrayList<Double> goalList = null;
                goalList = matchSelecter.getFirstGoalCount(temp.get("matchId"));
                print(goalList);
                if (goalList != null && (goalList.get(0) > 55 || goalList.get(1) > 55)) {
                    text = String.format("%s<p>%s</p><p>%s</p><p></p>", text, JSONObject.toJSONString(temp), JSONObject.toJSONString(goalList));
                } else {
                    continue;
                }
            }
            EmailText emailText = redisClient.get(key, EmailText.class);
            if (emailText == null) {
                emailText = new EmailText();
                emailText.setKey(key);
            }
            if (!head.equals(text) && !text.equals(emailText.getText())) {
                emailText.setStatus(0);
                emailText.setText(text);
            }
            redisClient.set(key, emailText);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * 比赛：未来三小时内，可能70min，大0.5
     *
     * @throws Exception
     */
    @Scheduled(fixedDelay = 3 * 60 * 60 * 1000)
    public void task1() {
        try {
            print(String.format("%s 00min match", new Date()));
            ArrayList<HashMap<String, String>> matchIdList = matchSelecter.get00minMatch();
            String key = "task1EmailText";
            String head = "<p><h2>未来三小时内，可能的25min或70min赛事</h2></p>";
            String text = head;
            for (Map<String, String> temp : matchIdList) {
                ArrayList<Integer> noGoalList = null;
                noGoalList = matchSelecter.getSecondNoGoalCount(temp.get("matchId"));
                if (noGoalList != null && (noGoalList.get(0) + noGoalList.get(1) >= 3)) {
                    text = String.format("%s<p>%s</p><p>%s</p><p></p>", text, JSONObject.toJSONString(temp), JSONObject.toJSONString(noGoalList));
                } else {
                    continue;
                }
            }
            for (Map<String, String> temp : matchIdList) {
                ArrayList<Double> goalList = null;
                goalList = matchSelecter.getFirstGoalCount(temp.get("matchId"));
                if (goalList != null && (goalList.get(0) > 55 || goalList.get(1) > 55)) {
                    text = String.format("%s<p>%s</p><p>%s</p><p></p>", text, JSONObject.toJSONString(temp), JSONObject.toJSONString(goalList));
                } else {
                    continue;
                }
            }
            EmailText emailText = redisClient.get(key, EmailText.class);
            if (emailText == null) {
                emailText = new EmailText();
                emailText.setKey(key);
            }
            if (!head.equals(text) && !text.equals(emailText.getText())) {
                emailText.setStatus(0);
                emailText.setText(text);
            }
            redisClient.set(key, emailText);
        } catch (Exception e) {
            return;
        }
    }

    private void print(Object object) {
        System.out.println(object);
    }

    private void sendEmail(final String subject, final String text) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    EmailMisc.sendEmail(subject, text);
                } catch (MessagingException e) {
                    print("send email fail...");
                }
            }
        });
    }

    @Scheduled(fixedDelay = 1 * 60 * 1000)
    public void sendEmailTask() {
        try {
            ArrayList<String> emailTextNameList = new ArrayList<>();
            emailTextNameList.add("task1EmailText");
            emailTextNameList.add("task2EmailText");
            emailTextNameList.add("task3EmailText");
            ArrayList<EmailText> emailTextList = new ArrayList<>();
            String text = "";
            for (String name : emailTextNameList) {
                EmailText emailText = redisClient.get(name, EmailText.class);
                if (emailText == null) {
                    //邮件内容对象为null，跳过
                    continue;
                }
                if (emailText.getStatus() == 1) {
                    //发送状态为"已发送"，跳过
                    continue;
                }
                text = String.format("%s%s", text, emailText.getText());
                emailTextList.add(emailText);
            }
            if (text != "") {
                EmailMisc.sendEmail("match task", text);
            }
            for (EmailText emailText : emailTextList) {
                emailText.setStatus(1);
                redisClient.set(emailText.getKey(), emailText);
            }
        } catch (Exception e) {
            return;
        }
    }
}