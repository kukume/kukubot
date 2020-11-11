package me.kuku.yuq.logic.impl;

import com.IceCreamQAQ.Yu.util.IO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ToolLogicImpl implements ToolLogic {
    private final String url = "https://www.mxnzp.com/api";
    private final String appId = "ghpgtsokjvkjdmlk";
    private final String appSecret = "N2hNMC93empxb0twUW1jd1FRbVVtQT09";
    private final String params = "&app_id=" + appId + "&app_secret=" + appSecret;
    private final String myApi = "https://api.kuku.me";
    @Override
    public String dogLicking() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("http://api.yyhy.me/tg.php?type=api");
        if (jsonObject.getInteger("code") == 1)
            return jsonObject.getString("date") + "\n" + jsonObject.getString("content");
        else return "获取失败！！";
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
    public String mouthOdor() throws IOException {
        return OkHttpUtils.getJson("https://s.nmsl8.club/getloveword?type=2").getString("content");
    }

    @Override
    public String mouthSweet() throws IOException {
        return OkHttpUtils.getJson("https://s.nmsl8.club/getloveword?type=1").getString("content");
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
    public String queryIp(String ip) throws IOException {
        String str = OkHttpUtils.getStr("http://ipaddr.cz88.net/data.php?ip=" + ip,
                OkHttpUtils.addUA(UA.PC));
        String[] split = str.split("'");
        // 1-ip  3 - addr 5 - ua
        return split[3];
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
        JSONObject jsonObject = OkHttpUtils.getJson("https://api.devopsclub.cn/api/icpquery?url=" + domain);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data.size() == 0) return "未找到该域名的备案信息";
        else {
            return "主办单位名称：" + data.getString("organizer_name") + "\n" +
                    "主办单位性质：" + data.getString("organizer_nature") + "\n" +
                    "网站备案/许可证号：" + data.getString("recording_license_number") + "\n" +
                    "网站名称：" + data.getString("site_name") + "\n" +
                    "网站首页网址：" + data.getString("site_index_url") + "\n" +
                    "审核时间：" + data.getString("review_time");
        }
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
                        .append("链接：").append(jsonObject.getString("url")).append("\n")
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
    public String restoreShortUrl(String url) throws IOException {
        if (!url.startsWith("http")) url = "http://" + url;
        Response response = OkHttpUtils.get(url);
        response.close();
        String location = response.header("Location");
        if (location != null) return location;
        else return "该链接不能再跳转了！";
    }

    @Override
    public Result<String> weather(String local, String cookie) throws IOException {
        String code = null;
        String id = null;
        String jsStr = OkHttpUtils.getStr("https://qq-web.cdn-go.cn/city-selector/41c008e0/app/index/dist/cdn/index.bundle.js");
        String jsonStr = BotUtils.regex("var y=c\\(\"[0-9a-z]{20}\"\\),p=", ",s=\\{name", jsStr);
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
                    city, weather, code, new Date().getTime(), wPic, city, weather, temperature, air);
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
            ip = BotUtils.regex("\\[", "\\]", result);
            if (ip == null) ip = BotUtils.regex("\\(", "\\)", result);
            if (ip == null) return "域名解析失败！！！";
            else ip = ip.trim();
            String time;
            time = BotUtils.regex("时间=", "ms", result);
            if (time == null) time = BotUtils.regex("time=", "ms", result);
            if (time == null) return "请求超时！！"; else time = time.trim();
            String ipInfo = queryIp(ip);
            return "====查询结果====\n" + "域名/IP：" + domain + "\n" +
                    "IP：" + ip + "\n" +
                    "延迟：" + time + "msg" + "\n" +
                    "位置：" + ipInfo;
        }else return "ping失败，请稍后再试！！";
    }

    @Override
    public Result<Map<String, String>> colorPicByLoLiCon(String apiKey, boolean isR18) throws IOException {
        int r18 = 0;
        if (isR18) r18 = 1;
        JSONObject jsonObject = OkHttpUtils.getJson("https://api.lolicon.app/setu/?apikey" + apiKey + "&r18=" + r18);
        switch (jsonObject.getInteger("code")){
            case 0:
                JSONObject dataJsonObject = jsonObject.getJSONArray("data").getJSONObject(0);
                Map<String, String> map = new HashMap<>();
                map.put("count", jsonObject.getString("quota"));
                map.put("time", jsonObject.getString("quota_min_ttl"));
                map.put("url", dataJsonObject.getString("url"));
                map.put("title", dataJsonObject.getString("title"));
                map.put("pid", dataJsonObject.getString("pid"));
                map.put("uid", dataJsonObject.getString("uid"));
                return Result.success(map);
            case 401: return Result.failure("APIKEY 不存在或被封禁", null);
            case 429: return Result.failure("达到调用额度限制，距离下一次恢复额度时间：" + jsonObject.getLong("quota_min_ttl") + "秒", null);
            default: return Result.failure(jsonObject.getString("msg"), null);
        }
    }

    @Override
    public byte[] piXivPicProxy(String url) throws IOException {
        return OkHttpUtils.getBytes(myApi + "/pixiv/picbyurl?url=" + URLEncoder.encode(url, "utf-8"));
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
    public byte[] creatQr(String content) throws IOException {
        return OkHttpUtils.getBytes("https://www.zhihu.com/qrcode?url=" + URLEncoder.encode(content, "utf-8"));
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
                sb.append(jsonObject.getString("name")).append("-").append(jsonObject.getString("title")).append("\n");
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
    public byte[] queryTime() throws IOException {
        String time = OkHttpUtils.getStr(myApi + "/time/hm");
        return OkHttpUtils.getBytes("https://u.iheit.com/images/time/" + time + ".jpg");
    }

    @Override
    public String queryVersion() throws IOException {
        String html = OkHttpUtils.getStr("https://github.com/kukume/kuku-bot/tags");
        Elements elements = Jsoup.parse(html).select(".Details .d-flex .commit-title a");
        return elements.get(0).text();
    }

    @Override
    public String music163cloud() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("http://api.heerdev.top/nemusic/random");
        return jsonObject.getString("text");
    }

    @Override
    public String searchQuestion(String question) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("http://api.xmlm8.com/tk.php?t=" + question);
        return "问题：" + jsonObject.getString("tm") + "\n" +
                "答案：" + jsonObject.getString("da");
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
    public List<Map<String, String>> zhiHuHot() {
        return null;
    }

    @Override
    public List<Map<String, String>> hostLocPost() throws IOException {
        String html = OkHttpUtils.getStr("https://www.hostloc.com/forum.php?mod=forumdisplay&fid=45&filter=author&orderby=dateline",
                OkHttpUtils.addUA(UA.PC));
        Elements elements = Jsoup.parse(html).getElementsByTag("tbody");
        List<Map<String, String>> list = new ArrayList<>();
        for (Element ele: elements){
            if (!ele.attr("id").startsWith("normalth")) continue;
            Element s = ele.getElementsByClass("s").first();
            String title = s.text();
            String url = "https://www.hostloc.com/" + s.attr("href");
            String name = ele.select("cite a").first().text();
            String time = ele.select("em a span").first().text();
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
    public byte[] danBooRuPic(String type) throws IOException {
        String tagHtml = OkHttpUtils.getStr("https://danbooru.donmai.us/", OkHttpUtils.addUA(UA.PC));
        Elements elements = Jsoup.parse(tagHtml).select("#tag-box ul li");
        List<Map<String, String>> tags = new ArrayList<>();
        elements.forEach(element -> {
            Map<String, String> map = new HashMap<>();
            map.put("tag", element.attr("data-tag-name"));
            map.put("num", element.getElementsByClass("post-count").first().attr("title"));
            tags.add(map);
        });
        Map<String, String> tagMap = null;
        if (type == null){
            tagMap = tags.get((int) (Math.random() * tags.size()));
        }else{
            for (Map<String, String> map: tags){
                if (type.equals(map.get("tag"))){
                    tagMap = map;
                    break;
                }
            }
        }
        if (tagMap == null) return null;
        String tag = tagMap.get("tag");
        int page = Integer.parseInt(tagMap.get("num")) / 20;
        if (page > 1000) page = 1000;
        int randomPage = (int) (Math.random() * page);
        String picHtml = OkHttpUtils.getStr(String.format("https://danbooru.donmai.us/posts?tags=%s&page=%d", tag, randomPage), OkHttpUtils.addUA(UA.PC));
        Elements picElements = Jsoup.parse(picHtml).select("#posts-container article");
        List<String> urls = new ArrayList<>();
        picElements.forEach(element -> urls.add(element.attr("data-file-url")));
        String url = urls.get((int) (Math.random() * urls.size()));
        return OkHttpUtils.getBytes(url);
    }

    @Override
    public String identifyPic(String url) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://saucenao.com/search.php?url=" + url + "&output_type=2");
        JSONArray jsonArray = jsonObject.getJSONArray("results");
        try {
            return jsonArray.getJSONObject(0).getJSONObject("data").getJSONArray("ext_urls").getString(0);
        }catch (NullPointerException e){
            return null;
        }
    }

    @Override
    public String githubQuicken(String gitUrl) {
        return "https://github.kuku.workers.dev/" + gitUrl;
    }

    @Override
    public String traceRoute(String domain) throws IOException {
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) return "不支持Windows系统！！";
        File file = new File("besttrace");
        Runtime runtime = Runtime.getRuntime();
        if (!file.exists()){
            byte[] bytes = OkHttpUtils.getBytes("https://u.iheit.com/kuku/bot/besttrace");
            IO.writeFile(file, bytes);
            runtime.exec("chmod +x besttrace");
        }
        Process process = runtime.exec("./besttrace " + domain);
        byte[] readBytes = IO.read(process.getInputStream(), true);
        return new String(readBytes, StandardCharsets.UTF_8);
    }

    @Override
    public String teachYou(String content, String type) throws IOException {
        String msg;
        String url;
        String suffix = URLEncoder.encode(Base64.getEncoder().encodeToString(content.getBytes()), "utf-8");
        switch (type){
            case "baidu":
                msg = "百度";
                url = "https://u.iheit.com/teachsearch/baidu/index.html?q=" + suffix;
                break;
            case "google":
                msg = "谷歌";
                url = "https://u.iheit.com/teachsearch/google/index.html?q=" + suffix;
                break;
            case "bing":
                msg = "必应";
                url = "https://u.iheit.com/teachsearch/bing/index.html?q=" + suffix;
                break;
            case "sougou":
                msg = "搜狗";
                url = "https://u.iheit.com/teachsearch/sougou/index.html?q=" + suffix;
                break;
            default: return null;
        }
        return "点击以下链接即可教您使用" + msg + "搜索“" + content + "“\n" + BotUtils.shortUrl(url);
    }

    @Override
    public String preventQQRed(String url) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        map.put("type", "2");
        map.put("dwz", "2");
        JSONObject jsonObject = OkHttpUtils.postJson("https://www.91she.cn/ajax.php?act=creat", map,
                OkHttpUtils.addUA(UA.PC));
        if (jsonObject.getInteger("code") == 0) return jsonObject.getString("dwz1");
        else return jsonObject.getString("msg");
    }

    @Override
    public String preventQQWechatRed(String url) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("http://fh.dw81.cn:81/dwz.php?type=ty&longurl=%s&dwzapi=500", url),
                OkHttpUtils.addUA(UA.PC));
        if (jsonObject.getInteger("code") == 1) return jsonObject.getString("ae_url");
        else return jsonObject.getString("msg");
    }
}
