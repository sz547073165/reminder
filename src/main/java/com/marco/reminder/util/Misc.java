package com.marco.reminder.util;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/12/4 0004.
 */
public class Misc {
    public static ArrayList<String> getStrByPattern(String patternStr, String string) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(string);
        ArrayList<String> targetList = new ArrayList<>();
        while (matcher.find()) {
            targetList.add(matcher.group());
        }
        return targetList;
    }
    public static String dateLong2String(Long timestamp, String format) {
        if (isEmpty(timestamp) || isEmpty(format)) {
            return null;
        }
        return new SimpleDateFormat(format).format(new Date(timestamp));
    }
    public static boolean isEmpty(Object object) {
        if (object instanceof String) {
            return StringUtils.isEmpty((String) object);
        }
        return object == null;
    }
    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    public static <T> boolean isListNotEmpty(List<T> list) {
        return list != null && list.isEmpty();
    }

    public static <T> boolean isListEmpty(List<T> list) {
        return !isListNotEmpty(list);
    }
}
