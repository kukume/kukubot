package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.CodeType;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;
import java.util.Map;

@AutoBind
public interface ToolLogic {
    String dogLicking() throws IOException;
    String baiKe(String text) throws IOException;
    String mouthOdor() throws IOException;
    String mouthSweet() throws IOException;
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
    String restoreShortUrl(String url) throws IOException;
    Result<String> weather(String local, String cookie) throws IOException;
    String ping(String domain) throws IOException;
    Result<Map<String, String>> colorPicByLoLiCon(String apiKey, boolean isR18) throws IOException;
    byte[] piXivPicProxy(String url) throws IOException;
    Map<String, String> hiToKoTo() throws IOException;
    byte[] creatQr(String content) throws IOException;
    String girlImage() throws IOException;
    String lolFree() throws IOException;
    String abbreviation(String content) throws IOException;
    byte[] queryTime() throws IOException;
    String queryVersion() throws IOException;
    String music163cloud() throws IOException;
    String searchQuestion(String question) throws IOException;
    Result<Map<String, String>> bvToAv(String bv) throws IOException;
    String wordSegmentation(String text) throws IOException;
    String acgPic() throws IOException;
    byte[] danBooRuPic(String type) throws IOException;
    String identifyPic(String url) throws IOException;
    String githubQuicken(String gitUrl);
    String traceRoute(String domain) throws IOException;
    String teachYou(String content, String type) throws IOException;
    String preventQQRed(String url) throws IOException;
    String songByQQ(String name) throws IOException;
    Result<String> songBy163(String name) throws IOException;
    String genShinUserInfo(long id) throws IOException;
    byte[] cosplay() throws IOException;
    byte[] photo() throws IOException;
    String uploadImage(byte[] bytes);
    String abstractWords(String word);
    String executeCode(String code, CodeType codeType) throws IOException;
    String urlToPic(String url) throws IOException;
}
