package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.pojo.CodeType;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@AutoBind
public interface ToolLogic {
    String dogLicking() throws IOException;
    String baiKe(String text) throws IOException;
    String poisonousChickenSoup() throws IOException;
    String loveWords() throws IOException;
    String saying() throws IOException;
    String queryIp(String ip) throws IOException;
    String queryWhois(String domain) throws IOException;
    String queryIcp(String domain) throws IOException;
    String zhiHuDaily() throws IOException;
    String qqGodLock(Long qq) throws IOException;
    String convertPinYin(String word) throws IOException;
    String jokes() throws IOException;
    String rubbish(String name) throws IOException;
    String historyToday() throws IOException;
    String convertZh(String content, Integer type) throws IOException;
    String convertTranslate(String content, String from, String to) throws IOException;
    String parseVideo(String url) throws IOException;
    Result<String> weather(String local, String cookie) throws IOException;
    String ping(String domain) throws IOException;
    Result<List<Map<String, String>>> colorPicByLoLiCon(String apiKey, boolean isR18) throws IOException;
    Map<String, String> hiToKoTo() throws IOException;
    InputStream creatQr(String content) throws IOException;
    String girlImage() throws IOException;
    String lolFree() throws IOException;
    String abbreviation(String content) throws IOException;
    String queryVersion() throws IOException;
    Result<Map<String, String>> bvToAv(String bv) throws IOException;
    String wordSegmentation(String text) throws IOException;
    String acgPic() throws IOException;
    String sauceNaoIdentifyPic(String apiKey, String url) throws IOException;
    String teachYou(String content, String type) throws IOException;
    String songByQQ(String name) throws IOException;
    Result<String> songBy163(String name) throws IOException;
    String uploadImage(InputStream is);
    String abstractWords(String word);
    String executeCode(String code, CodeType codeType) throws IOException;
    String urlToPic(String url) throws IOException;
    String pasteUbuntu(String poster, String syntax, String content);
    byte[] girlImageGaNk();
    JSONObject luckJson(int index) throws IOException;
    byte[] diu(String url);
    byte[] pa(String url);
    JSONArray loLiConQuickly(String tags) throws IOException;
    String qinYunKeChat(String message) throws IOException;
}
