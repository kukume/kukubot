package me.kuku.yuq.logic.impl;

import me.kuku.yuq.logic.HostLocLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class HostLocLogicImpl implements HostLocLogic {
    @Override
    public Result<String> login(String username, String password) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("fastloginfield", "username");
        map.put("username", username);
        map.put("cookietime", "2592000");
        map.put("password", password);
        map.put("quickforward", "yes");
        map.put("handlekey", "ls");
        Response response = OkHttpUtils.post("https://www.hostloc.com/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1",
                map, OkHttpUtils.addHeaders(null, "https://www.hostloc.com/forum.php", UA.PC));
        String str = OkHttpUtils.getStr(response);
        if (str.contains("https://www.hostloc.com/forum.php")){
            return Result.success(OkHttpUtils.getCookie(response));
        }else return Result.failure("账号或密码错误！", null);
    }

    @Override
    public boolean isLogin(String cookie) throws IOException {
        String html = OkHttpUtils.getStr("https://www.hostloc.com/home.php?mod=spacecp",
                OkHttpUtils.addHeaders(cookie, null, UA.PC));
        String text = Jsoup.parse(html).getElementsByTag("title").first().text();
        return text.contains("个人资料");
    }

    @Override
    public void sign(String cookie) throws IOException {
        List<String> urlList = new ArrayList<>();
        for (int i = 0; i < 12; i++){
            int num = BotUtils.randomInt(10000, 50000);
            urlList.add("https://www.hostloc.com/space-uid-" + num + ".html");
        }
        for (String url: urlList){
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            OkHttpUtils.get(url, OkHttpUtils.addHeaders(cookie, "https://www.hostloc.com/forum.php", UA.PC))
                    .close();
        }
    }

    @Override
    public List<Map<String, String>> post() {
        List<Map<String, String>> list = new ArrayList<>();
        String html;
        try {
            html = OkHttpUtils.getStr("https://www.hostloc.com/forum.php?mod=forumdisplay&fid=45&filter=author&orderby=dateline",
                    OkHttpUtils.addUA(UA.PC));
        } catch (IOException e) {
//            e.printStackTrace();
            return list;
        }
        Elements elements = Jsoup.parse(html).getElementsByTag("tbody");
        for (Element ele: elements){
            if (!ele.attr("id").startsWith("normalth")) continue;
            Element s = ele.getElementsByClass("s").first();
            String title = s.text();
            String url = "https://www.hostloc.com/" + s.attr("href");
            String name = ele.select("cite a").first().text();
            String time = null;
            try {
                time = ele.select("em a span").first().text();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String id = BotUtils.regex("tid=", "&", url);
            Map<String, String> map = new HashMap<>();
            map.put("title", title);
            map.put("url", url);
            map.put("name", name);
            map.put("time", time);
            map.put("id", id);
            list.add(map);
        }
        return list;
    }

}
