package com.marco.reminder.core;

import com.marco.reminder.util.HttpRequest;
import com.marco.reminder.util.Misc;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchSelecter {
    /**
     * 获取某场比赛的盘路情况，返回结果未处理
     *
     * @param matchId
     * @return
     * @throws Exception
     */
    private static String getPanLuResponse(String matchId) throws Exception {
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
    private static String getMatchesResponse() throws Exception {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Referer", "http://61.143.225.86:88/");
        headerMap.put("User-Agent", "Mozilla/3.0 (compatible; Indy Library)");
        headerMap.put("HeaderEnd", "CRLF");
        String r = "007" + new Date().getTime();
        String response = HttpRequest.doGet("http://61.143.225.86:88/vbsxml/bfdata.js?r=" + r, headerMap, "GBK");
        return response;
    }

    public static ArrayList<HashMap<String, String>> get70minMatchId() throws Exception {
        String response = getMatchesResponse();
        response = response.replaceAll("<.*?>", "");
        int matchCount = Integer.parseInt(response.split(";")[3].split("=")[1]);
        String[] tempMatchInfoString = response.split("\\);");
        ArrayList<HashMap<String, String>> matchIdList = new ArrayList<>();

        for (int i = 3; i < (matchCount + 3); i++) {
            String[] temp = tempMatchInfoString[i].split("\"");
            String matchStr;
            if (i == 3) {
                matchStr = temp[5];
            } else {
                matchStr = temp[1];
            }
            String[] match = matchStr.split("\\^");
            // 获取比赛半场开场时间
            Calendar halfTime = Calendar.getInstance();
            halfTime.set(Integer.parseInt(match[12].split(",")[0]), Integer.parseInt(match[12].split(",")[1]),
                    Integer.parseInt(match[12].split(",")[2]), Integer.parseInt(match[12].split(",")[3]),
                    Integer.parseInt(match[12].split(",")[4]));
            long halfTimeLong = halfTime.getTimeInMillis() / 1000;
            long nowLong = Instant.now().getEpochSecond();
            // 筛选进行时间大于70分钟，小于75分钟的比赛
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
                            matchIdList.add(map);
                        }
                    }
                }
            }
        }
        return matchIdList;
    }

    /**
     * 获取今天所有比赛<br>
     * 如：[{league=斯卡冠联, matchTeam=拉纳维SC VS 新青年, matchId=1483607},,,]
     *
     * @return
     * @throws Exception
     */
    public static List<Map<String, String>> getAllMatches() throws Exception {
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
     * 如：[1, 0]，1表示主场的数据，0表示客场的数据
     *
     * @param matchId
     * @return
     * @throws Exception
     */
    public static ArrayList<Integer> getNoGoalCount(String matchId) throws Exception {
        String response = getPanLuResponse(matchId);
        String panLuPatternStr = "var hometeamid.*?var GoalCn";
        Pattern panLuPattern = Pattern.compile(panLuPatternStr);
        Matcher panLuMatcher = panLuPattern.matcher(response);
        String targetStr = null;
        if (panLuMatcher.find()) {
            targetStr = panLuMatcher.group();
        }
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
        int homeSignal = checkSignal(panLuList, homeId);
        int guestSignal = checkSignal(panLuList, guestId);
        ArrayList<Integer> resultList = new ArrayList<>();
        resultList.add(homeSignal);
        resultList.add(guestSignal);
        return resultList;
    }

    private static int checkSignal(List<Map<String, String>> panLuList, String teamId) {
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

    /**
     * 根据matchId获取该比赛的欧赔数据
     *
     * @param matchesId
     * @return
     * @throws Exception
     */
    public static String getMatchesByMatchIdFromNet(String matchesId) throws Exception {
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
}
