package com.marco.reminder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/12/4 0004.
 */
public class Misc {
    public static List<String> getStrByPattern(String patternStr, String string) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(string);
        ArrayList<String> targetList = new ArrayList<>();
        while (matcher.find()) {
            targetList.add(matcher.group());
        }
        return targetList;
    }
}
