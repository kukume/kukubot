package me.kuku.yuq.logic.impl;

import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.util.IO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.UA;
import me.kuku.utils.IOUtils;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.CodeType;
import me.kuku.yuq.utils.*;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class ToolLogicImpl implements ToolLogic {
    private final String url = "https://www.mxnzp.com/api";
    private final String appId = "ghpgtsokjvkjdmlk";
    private final String appSecret = "N2hNMC93empxb0twUW1jd1FRbVVtQT09";
    private final String params = "&app_id=" + appId + "&app_secret=" + appSecret;
    @Config("YuQ.Mirai.bot.api")
    private String api;
    @Override
    public String dogLicking() throws IOException {
        // https://api.oick.cn/dog/api.php
        return OkHttpUtils.getStr("https://v1.alapi.cn/api/dog?format=text");
    }

    private Result<String> baiKeByUrl(String url) throws IOException {
        Response response = OkHttpUtils.get(url);
        while (response.code() == 302){
            response.close();
            String location = response.header("Location");
            assert location != null;
            if (location.startsWith("//baike.baidu.com/search/none")) return Result.failure("", null);
            String resultUrl;
            if (location.startsWith("//")) resultUrl = "https:" + location;
            else resultUrl = "https://baike.baidu.com" + location;
            response = OkHttpUtils.get(resultUrl);
        }
        String html = OkHttpUtils.getStr(response);
        Document doc = Jsoup.parse(html);
        try {
            String result = doc.select(".lemma-summary .para").first().text();
            return Result.success(result);
        }catch (NullPointerException e){
            return Result.failure(210, "https://baike.baidu.com" + doc.select("li[class=list-dot list-dot-paddingleft]").first().getElementsByTag("a").first().attr("href"));
        }
    }

    @Override
    public String baiKe(String text) throws IOException {
        String encodeText = URLEncoder.encode(text, "utf-8");
        String url = "https://baike.baidu.com/search/word?word=" + encodeText;
        Result<String> result = baiKeByUrl(url);
        Integer code = result.getCode();
        if (code == 200) return result.getData() + "\n查看详情：" + BotUtils.shortUrl(url);
        else if (code == 210) {
            String resultUrl = result.getData();
            return baiKeByUrl(resultUrl).getData() + "\n查看详情：" + BotUtils.shortUrl(resultUrl);
        }else return "抱歉，没有找到与“" + text + "”相关的百科结果。";
    }

    @Override
    public String poisonousChickenSoup() throws IOException {
        Response response = OkHttpUtils.get("https://v1.alapi.cn/api/soul");
        if (response.code() == 200){
            return OkHttpUtils.getJson(response).getJSONObject("data").getString("title");
        }else return "获取失败！";
    }

    @Override
    public String loveWords() throws IOException {
        return OkHttpUtils.getStr("https://v1.alapi.cn/api/qinghua?format=text");
    }

    @Override
    public String saying() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://v1.alapi.cn/api/mingyan");
        if (jsonObject.getInteger("code") == 200){
            JSONObject data = jsonObject.getJSONObject("data");
            return data.getString("content") + "-----" + data.getString("author");
        }else return "获取失败！";
    }

    @Override
    public Result<List<Map<String, String>>> queryIp(String ip) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(api + "/tool/ip?ip=" + ip);
        if (jsonObject.getInteger("code") == 200){
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            List<Map<String, String>> list = new ArrayList<>();
            jsonArray.stream().map(obj -> (JSONObject) obj).forEach(singleJsonObject -> {
                Map<String, String> map = new HashMap<>();
                map.put("ip", singleJsonObject.getString("ip"));
                map.put("address", singleJsonObject.getString("address"));
                list.add(map);
            });
            if (list.size() != 0)
                return Result.success(list);
            else return Result.failure("查询IP地址失败！");
        }else return Result.failure(jsonObject.getString("message"));
    }

    @Override
    public String queryWhois(String domain) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://api.devopsclub.cn/api/whoisquery?domain=" + domain + "&standard=true");
        JSONObject data = jsonObject.getJSONObject("data").getJSONObject("data");
        if (data.size() == 0) return "未找到该域名的whois信息";
        else {
            return "域名：" + data.getString("domainName") + "\n" +
                    "域名状态：" + data.getString("domainStatus") + "\n" +
                    "联系人：" + data.getString("registrant") + "\n" +
                    "联系邮箱：" + data.getString("contactEmail") + "\n" +
                    "注册商：" + data.getString("registrar") + "\n" +
                    "DNS：" + data.getString("dnsNameServer") + "\n" +
                    "创建时间：" + data.getString("registrationTime") + "\n" +
                    "过期时间：" + data.getString("expirationTime");
        }
    }

    @Override
    public String queryIcp(String domain) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(api + "/tool/icp?keyword=" + domain);
        if (jsonObject.getInteger("code") != 200) return "未找到该域名的备案信息";
        JSONObject data = jsonObject.getJSONArray("data").getJSONObject(0);
        return "主办单位名称：" + data.getString("unitName") + "\n" +
                "网站备案/许可证号：" + data.getString("licence") + "\n" +
                "网站名称：" + data.getString("name") + "\n" +
                "网站首页网址：" + data.getString("homeUrl") + "\n" +
                "最后更新时间：" + data.getString("updateTime");
    }

    @Override
    public String zhiHuDaily() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://v1.alapi.cn/api/zhihu/latest");
        if (jsonObject.getInteger("code") == 200){
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("stories");
            StringBuilder sb = new StringBuilder();
            jsonArray.forEach(obj -> {
                JSONObject json = (JSONObject) obj;
                sb.append("标题：").append(json.getString("title")).append("\n")
                        .append("链接：").append(json.getString("url")).append("\n")
                        .append(" -------------- ").append("\n");
            });
            return sb.toString();
        }else return "获取失败，" + jsonObject.getString("msg");
    }

    @Override
    public String qqGodLock(Long qq) throws IOException {
        Document doc = Jsoup.connect("http://qq.link114.cn/" + qq).get();
        Element ele = doc.getElementById("main").getElementsByClass("listpage_content").first();
        Elements elements = ele.getElementsByTag("dl");
        StringBuilder sb = new StringBuilder();
        for (Element element: elements){
            sb.append(element.getElementsByTag("dt").first().text())
                    .append(element.getElementsByTag("dd").text()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String convertPinYin(String word) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://v1.alapi.cn/api/pinyin?word=" + word + "&tone=1");
        if (jsonObject.getInteger("code") == 200) return jsonObject.getJSONObject("data").getString("pinyin");
        else return "转换失败！！";
    }

    String convertUrl(String path){
        return url + path + "?" + params;
    }

    @Override
    public String jokes() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(convertUrl("/jokes/list/random"));
        JSONArray data = jsonObject.getJSONArray("data");
        return data.getJSONObject((int) (Math.random() * data.size())).getString("content");
    }

    @Override
    public String rubbish(String name) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        JSONObject jsonObject = OkHttpUtils.postJson(convertUrl("/rubbish/type"), map);
        if (jsonObject.getInteger("code") == 0) return "没有这个垃圾";
        else {
            StringBuilder sb = new StringBuilder();
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject aim = data.getJSONObject("aim");
            if (aim != null) sb.append(aim.getString("goodsName")).append("；").append(aim.getString("goodsType"))
                    .append("\n");
            JSONArray recommendList = data.getJSONArray("recommendList");
            for (Object obj: recommendList){
                JSONObject singleJsonObject = (JSONObject) obj;
                sb.append(singleJsonObject.getString("goodsName")).append("；").append(singleJsonObject.getString("goodsType"));
            }
            return sb.toString();
        }
    }

    @Override
    public String historyToday() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(convertUrl("/history/today"));
        JSONArray data = jsonObject.getJSONArray("data");
        StringBuilder sb = new StringBuilder();
        for (Object obj: data){
            JSONObject singleJsonObject = (JSONObject) obj;
            sb.append(singleJsonObject.getString("year")).append("年")
                    .append(singleJsonObject.getInteger("month")).append("月")
                    .append(singleJsonObject.getInteger("day")).append("日，")
                    .append(singleJsonObject.getString("title")).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String convertZh(String content, Integer type) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("type", type.toString());
        JSONObject jsonObject = OkHttpUtils.postJson(convertUrl("/convert/zh"), map);
        return jsonObject.getJSONObject("data").getString("convertContent");
    }

    @Override
    public String convertTranslate(String content, String from, String to) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("from", from);
        map.put("to", to);
        JSONObject jsonObject = OkHttpUtils.postJson(convertUrl("/convert/translate"), map);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data == null) return jsonObject.getString("msg");
        else return data.getString("result");
    }

    @Override
    public String parseVideo(String url) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        JSONObject jsonObject = OkHttpUtils.postJson("https://api.devopsclub.cn/api/svp", map);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data.size() != 0)
            return "描述：" + data.getString("desc") + "\n图片：" +
                    BotUtils.shortUrl(data.getString("url")) + "\n视频：" +
                    BotUtils.shortUrl(data.getString("video")) + "\n音乐：" +
                    BotUtils.shortUrl(data.getString("music"));
        else return "没有找到该视频";
    }

    @Override
    public Result<String> weather(String local, String cookie) throws IOException {
        String code = null;
        String id = null;
        String jsStr = OkHttpUtils.getStr("https://qq-web.cdn-go.cn/city-selector/41c008e0/app/index/dist/cdn/index.bundle.js");
        String jsonStr = MyUtils.regex("var y=c\\(\"[0-9a-z]{20}\"\\),p=", ",s=\\{name", jsStr);
        JSONArray cityJsonArray = JSON.parseArray(jsonStr);
        for (Object obj: cityJsonArray){
            JSONObject jsonObject = (JSONObject) obj;
            if (local.equals(jsonObject.getString("district"))){
                id = jsonObject.getString("areaid");
                code = jsonObject.getString("adcode");
                break;
            }
        }
        if (code != null){
            String url = String.format("https://weather.mp.qq.com/?city=%s&areaid=%s&adcode=%s&star=8",
                    URLEncoder.encode(local, "utf-8"), id, code);
            Response response = OkHttpUtils.get(url, OkHttpUtils.addHeaders(cookie, null, UA.QQ2));
            if (response.code() == 302) return Result.failure("Cookie已失效！！", null);
            String html = OkHttpUtils.getStr(response);
            Document doc = Jsoup.parse(html);
            String city = doc.getElementById("s_city").text();
            String temperature = doc.select(".cur-weather-info .date span").first().text();
            String air = doc.select("._val").first().text();
            String weather = doc.getElementById("s_info1").getElementsByTag("span").first().text();
            String wPic;
            switch (weather){
                case "晴": wPic = "sub"; break;
                case "多云": wPic = "fine"; break;
                case "雨":
                case "阵雨": wPic = "rain"; break;
                case "阴":
                default: wPic = "cloud";
            }
            String xmlStr = String.format("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"146\" templateID=\"1\" action=\"web\" brief=\"[分享] %s %s\" sourcePublicUin=\"2658655094\" sourceMsgId=\"0\" url=\"https://weather.mp.qq.com/pages/aio?_wv=1090533159&amp;_wwv=196612&amp;scene=1&amp;adcode=%s&amp;timeStamp=%s\" flag=\"0\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"2\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://imgcache.qq.com/ac/qqweather/image/share_icon/%s.png\" w=\"0\" h=\"0\" /><title>%s %s</title><summary>%s\n" +
                    "空气质量:%s</summary></item><source name=\"QQ天气\" icon=\"https://url.cn/JS8oE7\" action=\"plugin\" a_actionData=\"mqqapi://app/action?pkg=com.tencent.mobileqq&amp;cmp=com.tencent.biz.pubaccount.AccountDetailActivity&amp;uin=2658655094\" i_actionData=\"mqqapi://card/show_pslcard?src_type=internal&amp;card_type=public_account&amp;uin=2658655094&amp;version=1\" appid=\"-1\" /></msg>",
                    city, weather, code, System.currentTimeMillis(), wPic, city, weather, temperature, air);
            return Result.success(xmlStr);
        }else return Result.failure("没有找到这个城市", null);
    }

    @Override
    public String ping(String domain) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String os = System.getProperty("os.name");
        String pingStr;
        if (os.contains("Windows")) pingStr = "ping " + domain + " -n 1";
        else pingStr = "ping " + domain + " -c 1";
        Process process = runtime.exec(pingStr);
        if (process != null){
            byte[] bytes = IO.read(process.getInputStream(), true);
            String result;
            if (os.contains("Windows")) result = new String(bytes, Charset.forName("gbk"));
            else result = new String(bytes, StandardCharsets.UTF_8);
            if (result.contains("找不到主机") || result.contains("Name or service not known")) return "域名解析失败！！";
            String ip;
            ip = MyUtils.regex("\\[", "\\]", result);
            if (ip == null) ip = MyUtils.regex("\\(", "\\)", result);
            if (ip == null) return "域名解析失败！！！";
            else ip = ip.trim();
            String time;
            time = MyUtils.regex("时间=", "ms", result);
            if (time == null) time = MyUtils.regex("time=", "ms", result);
            if (time == null) return "请求超时！！"; else time = time.trim();
            Result<List<Map<String, String>>> ipResult = queryIp(ip);
            String ipInfo;
            if (ipResult.isSuccess()){
                ipInfo = ipResult.getData().get(0).get("address");
            }else ipInfo = "查询IP地址失败！";
            return "====查询结果====\n" + "域名/IP：" + domain + "\n" +
                    "IP：" + ip + "\n" +
                    "延迟：" + time + "ms" + "\n" +
                    "位置：" + ipInfo;
        }else return "ping失败，请稍后再试！！";
    }

    @Override
    public Result<List<Map<String, String>>> colorPicByLoLiCon(String apiKey, boolean isR18) throws IOException {
        int r18 = 0;
        if (isR18) r18 = 1;
        JSONObject jsonObject = OkHttpUtils.getJson("https://api.lolicon.app/setu/?apikey=" + apiKey + "&r18=" + r18 + "&num=10");
        JSONArray dataJsonArray = jsonObject.getJSONArray("data");
        ExecutorUtils.execute(() -> {
            for (int i = 0; i < dataJsonArray.size(); i++) {
                JSONObject singleJsonObject = dataJsonArray.getJSONObject(0);
                Map<String, String> paramMap = new HashMap<>();
                paramMap.put("pid", singleJsonObject.getString("pid"));
                paramMap.put("p", singleJsonObject.getString("p"));
                paramMap.put("uid", singleJsonObject.getString("uid"));
                paramMap.put("title", singleJsonObject.getString("title"));
                paramMap.put("author", singleJsonObject.getString("author"));
                paramMap.put("url", singleJsonObject.getString("url"));
                JSONArray tagsJsonArray = singleJsonObject.getJSONArray("tags");
                StringBuilder tags = new StringBuilder();
                for (int num = 0; num < tagsJsonArray.size(); num++) {
                    String tag = tagsJsonArray.getString(num);
                    tags.append(tag).append("|");
                }
                paramMap.put("tags", tags.deleteCharAt(tags.length() - 1).toString());
                try {
                    OkHttpUtils.post("https://api.kuku.me/lolicon", paramMap,
                            OkHttpUtils.addUA(UA.PC)).close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        switch (jsonObject.getInteger("code")){
            case 0:
                List<Map<String, String>> list = new ArrayList<>();
                for (int i = 0; i < dataJsonArray.size(); i++) {
                    JSONObject singleJsonObject = dataJsonArray.getJSONObject(i);
                    Map<String, String> map = new HashMap<>();
                    map.put("count", jsonObject.getString("quota"));
                    map.put("time", jsonObject.getString("quota_min_ttl"));
                    map.put("url", singleJsonObject.getString("url"));
                    map.put("title", singleJsonObject.getString("title"));
                    map.put("pid", singleJsonObject.getString("pid"));
                    map.put("uid", singleJsonObject.getString("uid"));
                    list.add(map);
                }
                return Result.success(list);
            case 401: return Result.failure("APIKEY 不存在或被封禁", null);
            case 429: return Result.failure("达到调用额度限制，距离下一次恢复额度时间：" + jsonObject.getLong("quota_min_ttl") + "秒", null);
            default: return Result.failure(jsonObject.getString("msg"), null);
        }
    }

    @Override
    public Map<String, String> hiToKoTo() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://v1.hitokoto.cn/");
        Map<String, String> map = new HashMap<>();
        map.put("text", jsonObject.getString("hitokoto"));
        map.put("from", jsonObject.getString("from"));
        return map;
    }

    @Override
    public InputStream creatQr(String content) throws IOException {
        return OkHttpUtils.getByteStream("https://www.zhihu.com/qrcode?url=" + URLEncoder.encode(content, "utf-8"));
    }

    @Override
    public String girlImage() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(url + "/image/girl/list/random?" + params);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        return jsonArray.getJSONObject((int) (Math.random() * jsonArray.size())).getString("imageUrl");
    }

    @Override
    public String lolFree() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("http://game.gtimg.cn/images/lol/act/img/js/heroList/hero_list.js");
        JSONArray jsonArray = jsonObject.getJSONArray("hero");
        StringBuilder sb = new StringBuilder("LOL本周周免英雄如下：\n");
        for (Object obj: jsonArray){
            JSONObject singleJsonObject = (JSONObject) obj;
            if (singleJsonObject.getInteger("isWeekFree") == 1)
                sb.append(singleJsonObject.getString("name")).append("-").append(singleJsonObject.getString("title")).append("\n");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Override
    public String abbreviation(String content) throws IOException {
        String str = OkHttpUtils.postStr("https://lab.magiconch.com/api/nbnhhsh/guess",
                OkHttpUtils.addJson("{\"text\": \"" + content + "\"}"));
        JSONArray jsonArray = JSON.parseArray(str);
        if(jsonArray.size() > 0){
            JSONArray transJsonArray = jsonArray.getJSONObject(0).getJSONArray("trans");
            if (transJsonArray == null) return "没有查询到结果！！";
            StringBuilder sb = new StringBuilder("缩写" + content + "的含义如下：\n");
            for (Object obj: transJsonArray){
                sb.append(obj.toString()).append("\n");
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        }else return "没有查询到结果";
    }

    @Override
    public String queryVersion() throws IOException {
        String html = OkHttpUtils.getStr("https://github.com/kukume/kuku-bot/tags");
        Elements elements = Jsoup.parse(html).select(".Details .d-flex .commit-title a");
        return elements.get(0).text();
    }

    @Override
    public Result<Map<String, String>> bvToAv(String bv) throws IOException {
        if (bv.length() != 12) return Result.failure("不合格的bv号", null);
        JSONObject jsonObject = OkHttpUtils.getJson("https://api.bilibili.com/x/web-interface/view?bvid=" + bv);
        Integer code = jsonObject.getInteger("code");
        if (code == 0){
            JSONObject dataJsonObject = jsonObject.getJSONObject("data");
            Map<String, String> map = new HashMap<>();
            map.put("pic", dataJsonObject.getString("pic"));
            map.put("dynamic", dataJsonObject.getString("dynamic"));
            map.put("title", dataJsonObject.getString("title"));
            map.put("desc", dataJsonObject.getString("desc"));
            map.put("aid", dataJsonObject.getString("aid"));
            map.put("url", "https://www.bilibili.com/video/av" + dataJsonObject.getString("aid"));
            return Result.success(map);
        }else if (code == -404) return Result.failure("没有找到该BV号！！", null);
        else return Result.failure(jsonObject.getString("message"), null);
    }

    @Override
    public String wordSegmentation(String text) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://api.devopsclub.cn/api/segcut?text=" + URLEncoder.encode(text, "utf-8"));
        if (jsonObject.getInteger("code") == 0){
            StringBuilder sb = new StringBuilder();
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("result");
            jsonArray.forEach(obj -> sb.append(obj).append("\n"));
            return sb.deleteCharAt(sb.length() - 1).toString();
        }else return jsonObject.getString("msg");
    }

    @Override
    public String acgPic() throws IOException {
        Response response = OkHttpUtils.get("https://v1.alapi.cn/api/acg");
        response.close();
        return response.header("location");
    }

    @Override
    public String sauceNaoIdentifyPic(String apiKey, String url) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://saucenao.com/search.php?url=" + url + "&output_type=2&api_key=" + apiKey);
        JSONObject headerJsonObject = jsonObject.getJSONObject("header");
        if (headerJsonObject.getInteger("status") != 0) return headerJsonObject.getString("message");
        JSONArray jsonArray = jsonObject.getJSONArray("results");
        try {
            return jsonArray.getJSONObject(0).getJSONObject("data").getJSONArray("ext_urls").getString(0);
        }catch (NullPointerException e){
            return null;
        }
    }

    @Override
    public String teachYou(String content, String type) throws IOException {
        String msg;
        String url;
        String suffix = URLEncoder.encode(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)), "utf-8");
        switch (type){
            case "baidu":
                msg = "百度";
                url = "https://static.kukuqaq.com/teachsearch/baidu/index.html?q=" + suffix;
                break;
            case "google":
                msg = "谷歌";
                url = "https://static.kukuqaq.com/teachsearch/google/index.html?q=" + suffix;
                break;
            case "bing":
                msg = "必应";
                url = "https://static.kukuqaq.com/teachsearch/bing/index.html?q=" + suffix;
                break;
            case "sougou":
                msg = "搜狗";
                url = "https://static.kukuqaq.com/teachsearch/sougou/index.html?q=" + suffix;
                break;
            default: return null;
        }
        return "点击以下链接即可教您使用" + msg + "搜索“" + content + "“\n" + BotUtils.shortUrl(url);
    }

    @Override
    public String uploadImage(InputStream is) {
        MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "kukuapi",
                        OkHttpUtils.getStreamBody(is)).build();
        try {
            JSONObject jsonObject = OkHttpUtils.postJson(api + "/tool/upload", body);
            return jsonObject.getJSONObject("image").getString("url");
        } catch (IOException e) {
            e.printStackTrace();
            return "图片上传失败，请稍后再试！！";
        }
    }

    @Override
    public String songByQQ(String name) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?w=" + URLEncoder.encode(name, "utf-8") + "&format=json");
        JSONObject songJsonObject = jsonObject.getJSONObject("data").getJSONObject("song").getJSONArray("list").getJSONObject(0);
        String songName = songJsonObject.getString("songname");
        String author = songJsonObject.getJSONArray("singer").getJSONObject(0).getString("name");
        String mid = songJsonObject.getString("songmid");
        JSONObject secondJsonObject = OkHttpUtils.getJson("https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22" + mid + "%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%220%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%220%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D");
        JSONObject dataJsonObject = secondJsonObject.getJSONObject("req_0").getJSONObject("data");
        JSONObject urlJsonObject = dataJsonObject.getJSONArray("midurlinfo").getJSONObject(0);
        String suffixUrl = urlJsonObject.getString("purl");
        if ("".equals(suffixUrl)) suffixUrl = dataJsonObject.getString("testfile2g");
        String musicUrl = dataJsonObject.getJSONArray("sip").getString(0) + suffixUrl;
        String jumpUrl = "https://y.qq.com/n/yqq/song/" + mid + ".html";
        String html = OkHttpUtils.getStr(jumpUrl, OkHttpUtils.addUA(UA.PC));
        String imageUrl = Jsoup.parse(html).select(".main .mod_data .data__cover img").first().attr("src");
        return "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"2\" templateID=\"12345\" action=\"web\" brief=\"音乐分享[" + songName + "]\" sourceMsgId=\"0\" url=\"" + jumpUrl + "\" flag=\"0\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"2\"><audio cover=\"http:" + imageUrl +"\" src=\"" + musicUrl + "\" /><title>" + songName + "</title><summary>" + author + "</summary></item><source name=\"\" icon=\"\" action=\"\" appid=\"-1\" /></msg>";
    }

    @Override
    public Result<String> songBy163(String name) throws IOException {
        String url = "https://netease.kuku.me";
        JSONObject jsonObject = OkHttpUtils.getJson(url + "/search?keywords=" + URLEncoder.encode(name, "utf-8"));
        JSONObject resultJsonObject = jsonObject.getJSONObject("result");
        if (resultJsonObject.getInteger("songCount") != 0){
            JSONObject songJsonObject = resultJsonObject.getJSONArray("songs").getJSONObject(0);
            Integer id = songJsonObject.getInteger("id");
            JSONObject secondJsonObject = OkHttpUtils.getJson(url + "/song/url?id=" + id);
            String songUrl = secondJsonObject.getJSONArray("data").getJSONObject(0).getString("url");
            if (songUrl != null){
                String songName = songJsonObject.getString("name");
                String author = songJsonObject.getJSONArray("artists").getJSONObject(0).getString("name");
                String html = OkHttpUtils.getStr("https://y.music.163.com/m/song?id=" + id, OkHttpUtils.addUA(UA.MOBILE));
                String imageUrl = Jsoup.parse(html).select("meta[property=og:image]").first().attr("content");
                String jumpUrl = "https://music.163.com/song?id=" + id;
                return Result.success("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"2\" templateID=\"12345\" action=\"web\" brief=\"音乐分享[" + songName + "]\" sourceMsgId=\"0\" url=\"" + jumpUrl + "\" flag=\"0\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"2\"><audio cover=\"http:" + imageUrl +"\" src=\"" + songUrl + "\" /><title>" + songName + "</title><summary>" + author + "</summary></item><source name=\"\" icon=\"\" action=\"\" appid=\"-1\" /></msg>");
            }else return Result.failure("可能该歌曲没有版权或者无法下载！", null);
        }else return Result.failure("未找到该歌曲！！", null);
    }

    @Override
    public String abstractWords(String word) {
        ScriptEngine se = new ScriptEngineManager().getEngineByName("JavaScript");
        try {
            String str = OkHttpUtils.downloadStr("https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/3a0a9684-c12e-4a6d-91e7-651f99877750.js");
            se.eval(str);
            Object o = se.eval("chouxiang(\"" + word + "\")");
            return o.toString();
        } catch (ScriptException | IOException e) {
            e.printStackTrace();
            return "生成失败，请重试！！";
        }
    }

    @Override
    public String executeCode(String code, CodeType codeType) throws IOException {
        String type = codeType.getType();
        String html = OkHttpUtils.getStr("http://www.dooccn.com/" + type + "/", OkHttpUtils.addUA(UA.PC));
        String id = MyUtils.regex("langid = ", ";", html);
        Map<String, String> map = new HashMap<>();
        map.put("language", id);
        map.put("code", Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8)));
        map.put("stdin", "123\nhaha2\n");
        JSONObject jsonObject = OkHttpUtils.postJson("http://runcode-api2-ng.dooccn.com/compile2", map,
                OkHttpUtils.addHeaders(null, "http://www.dooccn.com/", UA.PC));
        return jsonObject.getString("output");
    }

    @Override
    public String urlToPic(String url) throws IOException {
        Response response = OkHttpUtils.get("https://www.iloveimg.com/zh-cn/html-to-image", OkHttpUtils.addUA(UA.PC));
        String cookie = OkHttpUtils.getCookie(response);
        String str = OkHttpUtils.getStr(response);
        String token = "Bearer " + MyUtils.regex("\"token\":\"", "\"", str);
        String taskId = MyUtils.regex("ilovepdfConfig.taskId = '", "';", str);
        MultipartBody uploadBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("task", taskId)
                .addFormDataPart("cloud_file", url).build();
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", token);
        headersMap.put("user-agent", UA.PC.getValue());
        Headers headers = OkHttpUtils.addHeaders(headersMap);
        JSONObject uploadJsonObject = OkHttpUtils.postJson("https://api1h.ilovepdf.com/v1/upload", uploadBody, headers);
        String serverFileName = uploadJsonObject.getString("server_filename");
        MultipartBody previewBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("server_filename", serverFileName)
                .addFormDataPart("task", taskId)
                .addFormDataPart("url", url)
                .addFormDataPart("view_width", "320")
                .addFormDataPart("to_format", "jpg")
                .addFormDataPart("block_ads", "false")
                .addFormDataPart("remove_popups", "false").build();
        JSONObject jsonObject = OkHttpUtils.postJson("https://api1h.ilovepdf.com/v1/preview", previewBody, headers);
        return "https://api1h.ilovepdf.com/thumbnails/" + jsonObject.getString("thumbnail");
    }

    @Override
    public String pasteUbuntu(String poster, String syntax, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("poster", poster);
        map.put("syntax", syntax);
        map.put("content", content);
        try {
            Response response = OkHttpUtils.post("https://paste.ubuntu.com/", map, OkHttpUtils.addUA(UA.PC));
            response.close();
            return "https://paste.ubuntu.com" + response.header("location");
        } catch (IOException e) {
            return "生成失败！！";
        }
    }

    @Override
    public byte[] girlImageGaNk() {
        try {
            JSONObject jsonObject = OkHttpUtils.getJson("https://gank.io/api/v2/random/category/Girl/type/Girl/count/1");
            String url = jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
            return OkHttpUtils.downloadBytes(url);
        } catch (IOException ioException) {
            return null;
        }
    }

    private JSONArray luckJson = null;

    @Override
    public JSONObject luckJson(int index){
        if(luckJson == null){
            InputStream is = null;
            try {
                is = this.getClass().getResourceAsStream("/db/luck.json");
                if (is != null) {
                    byte[] bytes = IO.read(is, true);
                    luckJson = JSON.parseArray(new String(bytes, StandardCharsets.UTF_8));
                }
            } finally {
                IOUtils.close(is);
            }
        }
        return luckJson.getJSONObject(index-1);
    }

    @Override
    public byte[] diu(String url){
        return drawImages(url, "images/diu.png");
    }

    @Override
    public byte[] pa(String url){
        return drawImages(url, "images/pa.jpg");
    }

    @SuppressWarnings({"IntegerDivisionInFloatingPointContext", "ConstantConditions"})
    public byte[] drawImages(String url, String resourceUrl) {
        int hdW = 146;
        InputStream is = null;
        try {
            is = OkHttpUtils.getByteStream(url);
            BufferedImage headImage = ImageIO.read(is);
            BufferedImage bgImage = ImageIO.read(getClass().getClassLoader().getResource(resourceUrl));

            //处理头像
            BufferedImage formatAvatarImage = new BufferedImage(hdW, hdW, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D graphics = formatAvatarImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
            //图片是一个圆型
            Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, hdW, hdW);
            //需要保留的区域
            graphics.setClip(shape);
            if ("images/diu.png".equals(resourceUrl)) {
                graphics.rotate(Math.toRadians(-50), hdW / 2, hdW / 2);
            }
            graphics.drawImage(headImage.getScaledInstance(hdW, hdW, Image.SCALE_SMOOTH), 0, 0, hdW, hdW, null);
            graphics.dispose();

            //重合图片
            Graphics2D graphics2D = bgImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//抗锯齿
            if ("images/diu.png".equals(resourceUrl)) {
                graphics2D.drawImage(formatAvatarImage, 110 - hdW / 2, 275 - hdW / 2, hdW, hdW, null);//头画背景上
            } else if ("images/pa.jpg".equals(resourceUrl)) {
                graphics2D.drawImage(formatAvatarImage, 2, 240, 56, 56, null);//头画背景上
            }
            graphics2D.dispose();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bgImage, "PNG", bos);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.close(is);
        }
    }

    @Override
    public JSONArray loLiConQuickly(String tags) throws IOException {
        String url = "https://api.kuku.me/lolicon/random?num=20";
        if (tags != null) url += "&tags=" + tags;
        JSONObject jsonObject = OkHttpUtils.getJson(url,
                OkHttpUtils.addSingleHeader("Accept", "application/json"));
        return jsonObject.getJSONArray("data");
    }

    @Override
    public String qinYunKeChat(String message) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + URLEncoder.encode(message, "utf-8"));
        return jsonObject.getString("content");
    }

    @Override
    public Map<String, String> queryCompanyInfo(String companyName) throws IOException {
        JSONObject jsonObject = OkHttpUtils.postJson("https://icredit.jd.com/search/common/queryEntCard",
                OkHttpUtils.addJson("{\"queryString\":\"" + companyName + "\"}"),
                OkHttpUtils.addHeaders(null, "https://icredit.jd.com/enterprise?name=" + URLEncoder.encode(companyName, "utf-8"), UA.PC));
        JSONObject data = jsonObject.getJSONObject("data");
        if (data == null) return null;
        Map<String, String> map = new HashMap<>();
        map.put("score", data.getString("score"));
        map.put("title", data.getString("title"));
        map.put("progress", data.getString("progress"));
        data.getJSONObject("companyInfo").forEach((str, obj) -> map.put(str, obj == null ? null : obj.toString()));
        return map;
    }

    @Override
    public List<String> searchCompany(String name) throws IOException {
        JSONObject jsonObject = OkHttpUtils.postJson("https://icredit.jd.com/search/common/entList",
                OkHttpUtils.addJson("{\"queryString\":\"" + name + "\",\"pageNum\":1,\"pageSize\":10}"),
                OkHttpUtils.addReferer("https://icredit.jd.com/result?name=" + URLEncoder.encode(name, "utf-8")));
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
        List<String> list = new ArrayList<>();
        jsonArray.stream().map(obj -> (JSONObject) obj).forEach(obj -> list.add(obj.getString("entName")));
        return list;
    }

    @Override
    public String girl() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://www.onexiaolaji.cn/RandomPicture/api/?class=jkfun&type=json");
        if (jsonObject.getInteger("code") == 200) return jsonObject.getString("url");
        else return jsonObject.getString("msg");
    }
}
