package com.marco.reminder.core;

import com.marco.reminder.util.HttpRequest;
import com.marco.reminder.util.Misc;
import com.marco.reminder.util.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MatchSelecter {
    @Autowired
    private RedisClient redisClient;

    /**
     * 获取某场比赛的盘路情况，返回结果未处理
     *
     * @param matchId
     * @return
     * @throws Exception
     */
    private String getPanLuResponse(String matchId) throws Exception {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Referer", "http://live.titan007.com/");
        headerMap.put("User-Agent", "Mozilla/3.0 (compatible; Indy Library)");
        //headerMap.put("HeaderEnd", "CRLF");
        String response = HttpRequest.doGet("http://bf.win007.com/panlu/" + matchId + ".htm", headerMap, "GBK");
        return response;
    }

    /**
     * 获取今天所有比赛，返回结果未处理
     *
     * @return
     * @throws Exception
     */
    private String getMatchesResponse() throws Exception {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Referer", "http://61.143.225.86:88/");
        headerMap.put("User-Agent", "Mozilla/3.0 (compatible; Indy Library)");
        headerMap.put("HeaderEnd", "CRLF");
        String r = "007" + new Date().getTime();
        String response = HttpRequest.doGet("http://61.143.225.86:88/vbsxml/bfdata.js?r=" + r, headerMap, "GBK");
        return response;
    }

    /**
     * 获取今天所有比赛<br>
     * 如：[{league=斯卡冠联, matchTeam=拉纳维SC VS 新青年, matchId=1483607},,,]
     *
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> getAllMatches() throws Exception {
        String response = getMatchesResponse();

        response = response.replaceAll("<.*?>", "");
        int matchCount = Integer.parseInt(response.split(";")[3].split("=")[1]);
        String[] tempMatchInfoString = response.split("\\);");

        List<Map<String, String>> matchesList = new ArrayList<>();

        for (int i = 3; i < (matchCount + 3); i++) {
            String[] temp = tempMatchInfoString[i].split("\"");
            String matchStr;
            if (i == 3) {
                matchStr = temp[5];
            } else {
                matchStr = temp[1];
            }
            String[] matchList = matchStr.split("\\^");
            Map<String, String> match = new HashMap<>();
            match.put("matchId", matchList[0]);
            match.put("league", matchList[2]);
            match.put("matchTeam", matchList[5] + " VS " + matchList[8]);
            matchesList.add(match);
        }
        return matchesList;
    }

    /**
     * 获取主队客队最近比赛，下半场未进球的场数<br>
     * 如：[1, 0]，1表示主场未进球场数，0表示客场未进球场数
     *
     * @param matchId
     * @return
     * @throws Exception
     */
    public ArrayList<Integer> getSecondNoGoalCount(String matchId) throws Exception {
        String response = getPanLuResponse(matchId);
        String panLuPatternStr = "var hometeamid.*?var GoalCn";
        Pattern panLuPattern = Pattern.compile(panLuPatternStr);
        Matcher panLuMatcher = panLuPattern.matcher(response);
        String targetStr = null;
        if (!panLuMatcher.find()) {
            return null;
        }
        targetStr = panLuMatcher.group();
        String[] panLuStrArray = targetStr.split(";");
        String sClass = panLuStrArray[0].split("'")[1];
        String homeId = Misc.getStrByPattern("[1-9]\\d*", panLuStrArray[0]).get(0);
        String guestId = Misc.getStrByPattern("[1-9]\\d*", panLuStrArray[0]).get(1);
        ArrayList<Map<String, String>> panLuList = new ArrayList<>();
        for (int i = 1; i < panLuStrArray.length - 1; i++) {
            String[] temp = panLuStrArray[i].split("\\[")[2].split("\\]")[0].split(",");
            Map<String, String> map = new HashMap<>();
            map.put("matchId", temp[0]);
            map.put("sClass", temp[1]);
            map.put("homeName", temp[4]);
            map.put("guestName", temp[5]);
            map.put("homeId", temp[6]);
            map.put("guestId", temp[7]);
            map.put("homeGoal", temp[8]);
            map.put("guestGoal", temp[9]);
            map.put("homeGoalH", temp[10]);
            map.put("guestGoalH", temp[11]);
            panLuList.add(map);
        }
        int homeSignal = checkSecondSignal(panLuList, homeId);
        int guestSignal = checkSecondSignal(panLuList, guestId);
        ArrayList<Integer> resultList = new ArrayList<>();
        resultList.add(homeSignal);
        resultList.add(guestSignal);
        return resultList;
    }

    /**
     * 统计某个队，下半场未进球场数
     *
     * @param panLuList
     * @param teamId
     * @return
     */
    private int checkSecondSignal(List<Map<String, String>> panLuList, String teamId) {
        int signal = 0;
        for (Map<String, String> temp : panLuList) {
            if (teamId.equals(temp.get("homeId")) || teamId.equals(temp.get("guestId"))) {
                if (teamId.equals(temp.get("homeId"))) {
                    Integer homeGoal = Integer.parseInt(temp.get("homeGoal"));
                    Integer homeGoalH = Integer.parseInt(temp.get("homeGoalH"));
                    if (homeGoal - homeGoalH == 0) {
                        signal++;
                    } else {
                        break;
                    }
                } else {
                    Integer guestGoal = Integer.parseInt(temp.get("guestGoal"));
                    Integer guestGoalH = Integer.parseInt(temp.get("guestGoalH"));
                    if (guestGoal - guestGoalH == 0) {
                        signal++;
                    } else {
                        break;
                    }
                }
            }
        }
        return signal;
    }

    public ArrayList<Double> getFirstGoalCount(String matchId) throws Exception {
        String response = getPanLuResponse(matchId);
        String panLuPatternStr = "var hometeamid.*?var GoalCn";
        Pattern panLuPattern = Pattern.compile(panLuPatternStr);
        Matcher panLuMatcher = panLuPattern.matcher(response);
        String targetStr = null;
        if (!panLuMatcher.find()) {
            return null;
        }
        targetStr = panLuMatcher.group();
        String[] panLuStrArray = targetStr.split(";");
        String sClass = panLuStrArray[0].split("'")[1];
        String homeId = Misc.getStrByPattern("[1-9]\\d*", panLuStrArray[0]).get(0);
        String guestId = Misc.getStrByPattern("[1-9]\\d*", panLuStrArray[0]).get(1);
        ArrayList<Map<String, String>> panLuList = new ArrayList<>();
        for (int i = 1; i < panLuStrArray.length - 1; i++) {
            String[] temp = panLuStrArray[i].split("\\[")[2].split("\\]")[0].split(",");
            Map<String, String> map = new HashMap<>();
            map.put("matchId", temp[0]);
            map.put("sClass", temp[1]);
            map.put("homeName", temp[4]);
            map.put("guestName", temp[5]);
            map.put("homeId", temp[6]);
            map.put("guestId", temp[7]);
            map.put("homeGoal", temp[8]);
            map.put("guestGoal", temp[9]);
            map.put("homeGoalH", temp[10]);
            map.put("guestGoalH", temp[11]);
            panLuList.add(map);
        }
        double homeSignal = checkFirstGoalOrLoseSignal(panLuList, homeId);
        double guestSignal = checkFirstGoalOrLoseSignal(panLuList, guestId);
        ArrayList<Double> resultList = new ArrayList<>();
        resultList.add(homeSignal);
        resultList.add(guestSignal);
        return resultList;
    }

    /**
     * 统计某个队，上半场进球场数比
     *
     * @param panLuList
     * @param teamId
     * @return
     */
    private double checkFirstGoalOrLoseSignal(List<Map<String, String>> panLuList, String teamId) {
        System.out.println(String.format("盘路list的size值：%s", panLuList.size()));
        if (panLuList.isEmpty()) {
            return 0;
        }
        int signal = 0;
        double result = 0;
        int count = 0;
        for (int i = 0; i < panLuList.size(); i++) {
            Map<String, String> temp = panLuList.get(i);
            if (teamId.equals(temp.get("homeId")) || teamId.equals(temp.get("guestId"))) {
                Integer homeGoalH = Integer.parseInt(temp.get("homeGoalH"));
                Integer guestGoalH = Integer.parseInt(temp.get("guestGoalH"));
                if (homeGoalH + guestGoalH > 0) {
                    signal++;
                }
                count++;
                result = (double) signal / count * 100;
            }
        }
        if (count < 10) {
            return 0;
        }
        System.out.println("盘路比值计算结束");
        return Double.parseDouble(String.format("%.2f", result));
    }

    /**
     * 根据matchId获取该比赛的欧赔数据
     *
     * @param matchesId
     * @return
     * @throws Exception
     */
    public String getMatchesByMatchIdFromNet(String matchesId) throws Exception {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Referer", "http://score.365rich.cn/");
        headerMap.put("User-Agent", "Mozilla/3.0 (compatible; Indy Library)");
        headerMap.put("HeaderEnd", "CRLF");
        String response = HttpRequest.doGet("http://1x2.nowscore.com/" + matchesId + ".js", headerMap, "utf-8");

        String gameStr = null;
        String gameDetailStr = null;
        String gamePatternStr = "var game=Array\\(\\\"(.*?)\\\"\\);";
        Pattern gamePattern = Pattern.compile(gamePatternStr);
        Matcher gameMatcher = gamePattern.matcher(response);
        if (gameMatcher.find()) {
            gameStr = gameMatcher.group(0);
        }
        String gameDetailPatternStr = "gameDetail=Array\\(\\\"(.*?)\\\"\\);";
        Pattern gameDetailPattern = Pattern.compile(gameDetailPatternStr);
        Matcher gameDetailMatcher = gameDetailPattern.matcher(response);
        if (gameDetailMatcher.find()) {
            gameDetailStr = gameDetailMatcher.group(0);
        }
        return response;
    }

    /**
     * 获取今天所有比赛，返回ArrayList<br>
     * 如：["1406357^#FF7000^澳洲甲^澳洲甲^AUS D1^墨尔本城^墨爾本城^M...",,,]
     *
     * @return
     * @throws Exception
     */
    public ArrayList<String> getMatchesStrList() throws Exception {
        String response = getMatchesResponse();
        response = response.replaceAll("<.*?>", "");
        ArrayList<String> tempMatchInfoString = Misc.getStrByPattern("A\\[\\d*\\]=\".*?\"", response);
        for (int i = 0; i < tempMatchInfoString.size(); i++) {
            tempMatchInfoString.set(i, tempMatchInfoString.get(i).split("\"")[1]);
        }
        return tempMatchInfoString;
    }

    /**
     * 筛选比赛，获取比赛信息<br>
     * 时间：未来三个小时内<br>
     *
     * @return
     */
    public ArrayList<HashMap<String, String>> get00minMatch() {
        ArrayList<String> matchesStrList = redisClient.get("matchesStrList", ArrayList.class);
        ArrayList<HashMap<String, String>> matchList = new ArrayList<>();
        for (String temp : matchesStrList) {
            String[] match = temp.split("\\^");
            // 获取比赛上半场开始时间
            Calendar startTime = Calendar.getInstance();
            startTime.set(Integer.parseInt(match[12].split(",")[0]), Integer.parseInt(match[12].split(",")[1]),
                    Integer.parseInt(match[12].split(",")[2]), Integer.parseInt(match[11].split(":")[0]),
                    Integer.parseInt(match[11].split(":")[1]));
            long startTimeLong = startTime.getTimeInMillis() / 1000;
            long nowLong = Instant.now().getEpochSecond();
            // 还未进行的比赛
            if (nowLong < startTimeLong && startTimeLong < nowLong + (3 * 60 * 60)) {
                HashMap<String, String> map = new HashMap<>();
                map.put("matchId", match[0]);
                map.put("league", match[2]);
                map.put("startTime", Misc.dateLong2String(startTime.getTimeInMillis(), "HH:mm"));
                map.put("matchTeam", match[5] + " VS " + match[8]);
                matchList.add(map);
            }
        }
        return matchList;
    }

    /**
     * 筛选比赛，获取比赛信息<br>
     * 时间：25min<br>
     * 比分：0-0<br>
     * 其他：无红卡<br>
     *
     * @return
     */
    public ArrayList<HashMap<String, String>> get25minMatch() {
        ArrayList<String> matchesStrList = redisClient.get("matchesStrList", ArrayList.class);
        ArrayList<HashMap<String, String>> matchList = new ArrayList<>();
        for (String temp : matchesStrList) {
            String[] match = temp.split("\\^");
            // 获取比赛上半场开始时间
            Calendar startTime = Calendar.getInstance();
            startTime.set(Integer.parseInt(match[12].split(",")[0]), Integer.parseInt(match[12].split(",")[1]),
                    Integer.parseInt(match[12].split(",")[2]), Integer.parseInt(match[11].split(":")[0]),
                    Integer.parseInt(match[11].split(":")[1]));
            long startTimeLong = startTime.getTimeInMillis() / 1000;
            long nowLong = Instant.now().getEpochSecond();
            // 开场25min的比赛
            if (startTimeLong + 25 * 60 < nowLong && nowLong < startTimeLong + (25 + 5) * 60) {
                // 获取半场比分为0-0的比赛
                int nowGoalA = Integer.parseInt(match[14].isEmpty() ? "-2" : match[14]);
                int nowGoalB = Integer.parseInt(match[15].isEmpty() ? "-2" : match[15]);
                if (nowGoalA == 0 && nowGoalB == 0) {
                    // 红牌等于0
                    int redCard1 = Integer.parseInt(match[18]);
                    int redCard2 = Integer.parseInt(match[19]);
                    if (redCard1 == 0 && redCard2 == 0) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("matchId", match[0]);
                        map.put("league", match[2]);
                        map.put("matchTeam", match[5] + " VS " + match[8]);
                        matchList.add(map);
                    }
                }
            }
        }
        return matchList;
    }

    /**
     * 筛选比赛，获取比赛信息<br>
     * 时间：70min<br>
     * 比分：上半场0-0或1-1，下半场尚未进球<br>
     * 其他：无红卡<br>
     *
     * @return
     */
    public ArrayList<HashMap<String, String>> get70minMatch() {
        ArrayList<String> matchesStrList = redisClient.get("matchesStrList", ArrayList.class);
        ArrayList<HashMap<String, String>> matchList = new ArrayList<>();

        for (String temp : matchesStrList) {
            String[] match = temp.split("\\^");
            // 获取比赛半场开场时间
            Calendar halfTime = Calendar.getInstance();
            halfTime.set(Integer.parseInt(match[12].split(",")[0]), Integer.parseInt(match[12].split(",")[1]),
                    Integer.parseInt(match[12].split(",")[2]), Integer.parseInt(match[12].split(",")[3]),
                    Integer.parseInt(match[12].split(",")[4]));
            long halfTimeLong = halfTime.getTimeInMillis() / 1000;
            long nowLong = Instant.now().getEpochSecond();
            // 筛选进行时间大于72分钟，小于77分钟的比赛
            if (nowLong > halfTimeLong + 25 * 60 && nowLong < halfTimeLong + (25 + 5) * 60) {
                // if (true) {
                // 获取半场比分为0-0或1-1的比赛
                int halfGoalA = Integer.parseInt(match[16].isEmpty() ? "-1" : match[16]);
                int halfGoalB = Integer.parseInt(match[17].isEmpty() ? "-1" : match[17]);
                if ((halfGoalA == 0 && halfGoalB == 0) || (halfGoalA == 1 && halfGoalB == 1)) {
                    // 获取目前得分为0-0或1-1的比赛
                    int nowGoalA = Integer.parseInt(match[14].isEmpty() ? "-2" : match[14]);
                    int nowGoalB = Integer.parseInt(match[15].isEmpty() ? "-2" : match[15]);
                    if ((halfGoalA == nowGoalA && halfGoalB == nowGoalB)) {
                        // 红牌等于0
                        int redCard1 = Integer.parseInt(match[18]);
                        int redCard2 = Integer.parseInt(match[19]);
                        if (redCard1 == 0 && redCard2 == 0) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("matchId", match[0]);
                            map.put("league", match[2]);
                            map.put("matchTeam", match[5] + " VS " + match[8]);
                            map.put("matchGoal", match[14] + " : " + match[15]);
                            matchList.add(map);
                        }
                    }
                }
            }
        }
        return matchList;
    }
}
