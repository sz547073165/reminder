package com.marco.reminder.core;

import com.alibaba.fastjson.JSONObject;
import com.marco.reminder.Tester;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchSelecterTest extends Tester {
    @Autowired
    private MatchSelecter matchSelecter;

    @Test
    public void getMatchesStrList() throws Exception {
        ArrayList<String> matchesStrList = matchSelecter.getMatchesStrList();
        print(matchesStrList);
        print(matchesStrList.size());
    }

    @Test
    public void get70minMatch() throws Exception {
        print(matchSelecter.get70minMatch());
    }

    @Test
    public void getAllMatches() throws Exception {
    }

    @Test
    public void getNoGoalCount() throws Exception {
    }

    @Test
    public void getMatchesByMatchIdFromNet() throws Exception {
    }

    //@Test
    public void getFirstGoalCount() throws Exception {
        List<Map<String, String>> list = matchSelecter.getAllMatches();
        print(matchSelecter.getFirstGoalCount(list.get(5).get("matchId")));
    }

    @Test
    public void get25minMatch() throws Exception {
        ArrayList<HashMap<String, String>> list = matchSelecter.get25minMatch();
        print(list);
        HashMap<String, String> temp1 = new HashMap<>();
        temp1.put("matchId", "1484494");
        HashMap<String, String> temp2 = new HashMap<>();
        temp2.put("matchId", "1484934");
        list.add(temp1);
        list.add(temp2);
        String listStr = "[{league=哥斯甲附, startTime=Thu Dec 14 10:30:06 CST 2017, matchTeam=萨普里萨 VS 希雷迪亚诺, matchId=1484494}, {league=墨西丙, startTime=Thu Dec 14 10:30:06 CST 2017, matchTeam=伊拉普阿托 VS 帕蒂特兰德莫雷洛斯, matchId=1484934}]";
        String str = "";
        for (Map<String, String> temp : list) {
            ArrayList<Double> goalList = null;
            goalList = matchSelecter.getFirstGoalCount(temp.get("matchId"));
            print(goalList);
            if (goalList != null && (goalList.get(0) > 55 || goalList.get(1) > 55)) {
                str = String.format("%s<p>%s</p><p>%s</p><p></p>", str, JSONObject.toJSONString(temp), JSONObject.toJSONString(goalList));
            } else {
                continue;
            }
        }
        print(str);
    }

}