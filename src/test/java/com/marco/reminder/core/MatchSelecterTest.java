package com.marco.reminder.core;

import com.marco.reminder.Tester;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

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

}