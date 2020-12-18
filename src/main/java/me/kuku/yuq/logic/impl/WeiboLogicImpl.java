package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.entity.WeiboEntity;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.WeiboPojo;
import me.kuku.yuq.pojo.WeiboToken;
import me.kuku.yuq.utils.*;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class WeiboLogicImpl implements WeiboLogic {
    @Override
    public List<String> hotSearch() throws IOException {
        Document doc = Jsoup.connect("https://s.weibo.com/top/summary").get();
        Elements elements = doc.getElementById("pl_top_realtimehot").getElementsByTag("tbody").first()
                .getElementsByTag("tr");
        List<String> list = new ArrayList<>();
        for (Element ele: elements){
            String text = ele.getElementsByClass("td-01").first().text();
            if ("".equals(text)) text = "顶";
            String title = ele.getElementsByClass("td-02").first().getElementsByTag("a").first().text();
            list.add(text + "、" + title);
        }
        return list;
    }

    @Override
    public Result<List<WeiboPojo>> getIdByName(String name) throws IOException {
        String newName = URLEncoder.encode(name, "utf-8");
        Response response = OkHttpUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D3%26q%3D" + newName + "%26t%3D0&page_type=searchall",
                OkHttpUtils.addReferer("https://m.weibo.cn/search?containerid=100103type%3D1%26q%3D" + newName));
        if (response.code() == 200){
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            JSONArray cardsJsonArray = jsonObject.getJSONObject("data").getJSONArray("cards");
            JSONArray jsonArray = null;
            for (Object obj: cardsJsonArray){
                JSONObject singleJsonObject = (JSONObject) obj;
                JSONArray cardGroupJsonArray = singleJsonObject.getJSONArray("card_group");
                if (cardGroupJsonArray != null){
                    jsonArray = cardGroupJsonArray;
                    break;
                }
            }
            if (jsonArray == null) return Result.failure("没有找到该用户！！", null);
            List<WeiboPojo> list = new ArrayList<>();
            for (Object obj: jsonArray){
                JSONObject newJsonObject = (JSONObject) obj;
                if (newJsonObject.containsKey("user") || newJsonObject.containsKey("users")) {
                    JSONObject userJsonObject = newJsonObject.getJSONObject("user");
                    if (userJsonObject != null) {
                        String username = userJsonObject.getString("name");
                        if (username == null) username = userJsonObject.getString("screen_name");
                        list.add(new WeiboPojo(username, userJsonObject.getString("id")));
                    }else {
                        JSONArray usersJsonArray = newJsonObject.getJSONArray("users");
                        for (int i = 0; i < usersJsonArray.size(); i++){
                            JSONObject singleJsonObject = usersJsonArray.getJSONObject(i);
                            String username = singleJsonObject.getString("name");
                            if (username == null) username = singleJsonObject.getString("screen_name");
                            list.add(new WeiboPojo(username, singleJsonObject.getString("id")));
                        }
                    }
                }
            }
            if (list.size() == 0) return Result.failure("未找到该用户", null);
            else return Result.success(list);
        }else return Result.failure("查询失败，请稍后再试！！", null);
    }

    private WeiboPojo convert(JSONObject jsonObject){
        WeiboPojo weiboPojo = new WeiboPojo();
        JSONObject userJsonObject = jsonObject.getJSONObject("user");
        weiboPojo.setId(Long.parseLong(jsonObject.getString("id")));
        weiboPojo.setName(userJsonObject.getString("screen_name"));
        weiboPojo.setCreated(jsonObject.getString("created_at"));
        weiboPojo.setText(Jsoup.parse(jsonObject.getString("text")).text());
        weiboPojo.setBid(jsonObject.getString("bid"));
        weiboPojo.setUserId(userJsonObject.getString("id"));
        Integer picNum = jsonObject.getInteger("pic_num");
        if (picNum != 0){
            List<String> list = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray("pics");
            if (jsonArray != null){
                jsonArray.forEach(obj -> {
                    JSONObject picJsonObject = (JSONObject) obj;
                    String url = picJsonObject.getJSONObject("large").getString("url");
                    list.add(url);
                });
            }
            weiboPojo.setImageUrl(list);
        }
        if (jsonObject.containsKey("retweeted_status")){
            JSONObject forwardJsonObject = jsonObject.getJSONObject("retweeted_status");
            weiboPojo.setIsForward(true);
            weiboPojo.setForwardId(forwardJsonObject.getString("id"));
            weiboPojo.setForwardTime(forwardJsonObject.getString("created_at"));
            JSONObject forwardUserJsonObject = forwardJsonObject.getJSONObject("user");
            String name = null;
            if (forwardUserJsonObject == null) name = "原微博已删除";
            else forwardUserJsonObject.getString("screen_name");
            weiboPojo.setForwardName(name);
            weiboPojo.setForwardText(Jsoup.parse(forwardJsonObject.getString("text")).text());
            weiboPojo.setForwardBid(forwardJsonObject.getString("bid"));
        }else weiboPojo.setIsForward(false);
        return weiboPojo;
    }

    @Override
    public String convertStr(WeiboPojo weiboPojo) {
        StringBuilder sb = new StringBuilder();
        sb.append(weiboPojo.getName()).append("\n")
                .append("发布时间：").append(weiboPojo.getCreated()).append("\n")
                .append("内容：").append(weiboPojo.getText()).append("\n")
                .append("链接：").append("https://m.weibo.cn/status/").append(weiboPojo.getBid());
        if (weiboPojo.getIsForward()){
            sb.append("\n")
                    .append("转发自：").append(weiboPojo.getForwardName()).append("\n")
                    .append("发布时间：").append(weiboPojo.getForwardTime()).append("\n")
                    .append("内容：").append(weiboPojo.getForwardText()).append("\n")
                    .append("链接：").append("https://m.weibo.cn/status/").append(weiboPojo.getForwardBid());
        }
        return sb.toString();
    }

    @Override
    public Result<List<WeiboPojo>> getWeiboById(String id) throws IOException {
        Response response = OkHttpUtils.get(String.format("https://m.weibo.cn/api/container/getIndex?type=uid&uid=%s&containerid=107603%s", id, id));
        if (response.code() == 200){
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            JSONArray cardJsonArray = jsonObject.getJSONObject("data").getJSONArray("cards");
            List<WeiboPojo> list = new ArrayList<>();
            for (Object o : cardJsonArray) {
                JSONObject singleJsonObject = (JSONObject) o;
                JSONObject blogJsonObject = singleJsonObject.getJSONObject("mblog");
                if (blogJsonObject == null) continue;
                if (1 == blogJsonObject.getInteger("isTop")) continue;
                WeiboPojo weiboPojo = convert(blogJsonObject);
                list.add(weiboPojo);
            }
            return Result.success(list);
        }else return Result.failure("查询失败，请稍后再试！！", null);
    }

    private Map<String, String> loginParams(String username) throws IOException {
        Response response = OkHttpUtils.get(String.format("https://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su=%s&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.19)&_=%s",
                URLEncoder.encode(username, "utf-8"), new Date().getTime()));
        JSONObject jsonObject = OkHttpUtils.getJsonp(response);
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, Object> entry: jsonObject.entrySet()){
            map.put(entry.getKey(), entry.getValue().toString());
        }
        map.put("cookie", OkHttpUtils.getCookie(response) + "ULOGIN_IMG=" + map.get("pcid") + "; ");
        map.put("username", username);
        return map;
    }

    @Override
    public String getCaptchaUrl(String pcId){
        return "https://login.sina.com.cn/cgi/pin.php?r=" + BotUtils.randomNum(8) + "&s=0&p=" + pcId;
    }

    private String encryptPassword(Map<String, String> map, String password){
        String message =  map.get("servertime") + "\t" + map.get("nonce") + "\n" + password;
        try {
            password = RSAUtils.encrypt(message, RSAUtils.getPublicKey(map.get("pubkey"), "10001"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] bytes = Base64.getDecoder().decode(password);
        return HexUtils.bytesToHexString(bytes);
    }

    private String getMobileCookie(String pcCookie) throws IOException {
        Response response = OkHttpUtils.get("https://login.sina.com.cn/sso/login.php?url=https%3A%2F%2Fm.weibo.cn%2F%3F%26jumpfrom%3Dweibocom&_rand=1588483688.7261&gateway=1&service=sinawap&entry=sinawap&useticket=1&returntype=META&sudaref=&_client_version=0.6.33",
                OkHttpUtils.addCookie(pcCookie));
        response.close();
        return OkHttpUtils.getCookie(response);
    }

    @Override
    public Result<Map<String, String>> login(Map<String, String> map, String door) throws IOException {
        String newPassword = encryptPassword(map, map.get("password"));
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("entry", "weibo");
        paramMap.put("gateway", "1");
        paramMap.put("from", "");
        paramMap.put("savestate", "7");
        paramMap.put("qrcode_flag", "false");
        paramMap.put("useticket", "1");
        paramMap.put("pagerefer", "https://passport.weibo.com/visitor/visitor?entry=miniblog&a=enter&url=https%3A%2F%2Fweibo.com%2F&domain=.weibo.com&ua=php-sso_sdk_client-0.6.36&_rand=1596261779.2657");
        String pcId = "";
        if (door != null) pcId = map.get("pcid");
        paramMap.put("pcid", pcId);
        if (door == null) door = "";
        paramMap.put("door", door);
        paramMap.put("vnf", "1");
        paramMap.put("su", map.get("username"));
        paramMap.put("service", "miniblog");
        paramMap.put("servertime", map.get("servertime"));
        paramMap.put("nonce", map.get("nonce"));
        paramMap.put("pwencode", "rsa2");
        paramMap.put("rsakv", map.get("rsakv"));
        paramMap.put("sp", newPassword);
        paramMap.put("sr", "1536*864");
        paramMap.put("encoding", "UTF-8");
        paramMap.put("prelt", "55");
        paramMap.put("url", "https://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack");
        paramMap.put("returntype", "META");
        Response response = OkHttpUtils.post("https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.19)&_=" + new Date().getTime(),
                paramMap, OkHttpUtils.addCookie(map.get("cookie")));
        String html = OkHttpUtils.getCookie(response);
        String url = BotUtils.regex("location.replace\\(\"", "\"\\);", html);
        if (url == null) return Result.failure("获取失败！！", null);
        String token = BotUtils.regex("token%3D", "\"", html);
        String cookie = OkHttpUtils.getCookie(response);
        map.put("cookie", cookie);
        if (url.contains("https://login.sina.com.cn/crossdomain2.php")){
            map.put("url", url);
            map.put("referer", "https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.19)");
            return Result.success(map);
        }else if (token == null){
            String reason = BotUtils.regex("reason=", "&", html);
            String result = URLDecoder.decode(reason, "gbk");
            return Result.failure(result, null);
        }else {
            // 账号需要验证
            String phoneHtml = OkHttpUtils.getStr("https://login.sina.com.cn/protection/index?token=" + token + "&callback_url=https%3A%2F%2Fweibo.com");
            String phone = Jsoup.parse(phoneHtml).getElementById("ss0").attr("value");
            Map<String, String> phoneMap = new HashMap<>();
            phoneMap.put("encrypt_mobile", phone);
            JSONObject smsJsonObject = OkHttpUtils.postJson("https://login.sina.com.cn/protection/mobile/sendcode?token=" + token, phoneMap);
            if (smsJsonObject.getInteger("retcode") == 20000000){
                map.put("token", token);
                map.put("phone", phone);
                return Result.failure(201, "请输入短信验证码！！");
            }else return Result.failure(smsJsonObject.getString("msg"), null);
        }
    }

    @Override
    public WeiboEntity loginSuccess(String cookie, String referer, String url) throws IOException {
        String html = OkHttpUtils.getStr(url, OkHttpUtils.addHeaders(cookie, referer));
        String jsonStr = BotUtils.regex("sinaSSOController.setCrossDomainUrlList\\(", "\\);", html);
        JSONObject urlJsonObject = JSON.parseObject(jsonStr);
        String pcUrl = urlJsonObject.getJSONArray("arrURL").getString(0);
        Response pcResponse = OkHttpUtils.get(pcUrl + "&callback=sinaSSOController.doCrossDomainCallBack&scriptId=ssoscript0&client=ssologin.js(v1.4.19)&_=" + new Date().getTime());
        pcResponse.close();
        String pcCookie = OkHttpUtils.getCookie(pcResponse);
        String mobileCookie = getMobileCookie(cookie);
        return new WeiboEntity(pcCookie, mobileCookie);
    }

    @Override
    public Result<Map<String, String>> preparedLogin(String username, String password) throws IOException {
        String newUsername = Base64.getEncoder().encodeToString(username.getBytes());
        Map<String, String> loginParams = loginParams(newUsername);
        loginParams.put("password", password);
        if ("0".equals(loginParams.get("showpin"))) return Result.success(loginParams);
        else return Result.failure(201, "需要验证码！！", loginParams);
    }

    @Override
    public Result<WeiboEntity> loginBySms(String token, String phone, String code) throws IOException {
        String refererUrl = "https://login.sina.com.cn/protection/mobile/confirm?token=" + token;
        Map<String, String> map = new HashMap<>();
        map.put("encrypt_mobile", phone);
        map.put("code", code);
        JSONObject jsonObject = OkHttpUtils.postJson(refererUrl, map);
        switch (jsonObject.getInteger("retcode")){
            case 20000000:
                String url = jsonObject.getJSONObject("data").getString("redirect_url");
                Response resultResponse = OkHttpUtils.get(url, OkHttpUtils.addReferer(refererUrl));
                String cookie = OkHttpUtils.getCookie(resultResponse);
                String html = OkHttpUtils.getStr(resultResponse);
                String secondUrl = BotUtils.regex("location.replace\\(\"", "\"\\);", html);
                if (secondUrl == null) return Result.failure("登录失败，请稍后再试！！", null);
                return Result.success(loginSuccess(cookie, url, secondUrl));
            case 8518: return Result.failure("验证码错误或已经过期！！！", null);
            default: return Result.failure(jsonObject.getString("msg"), null);
        }
    }

    @Override
    public Result<WeiboEntity> loginByQQ(QQLoginEntity qqLoginEntity) throws IOException {
        Response startWeiboResponse = OkHttpUtils.get("https://passport.weibo.com/othersitebind/authorize?entry=miniblog&site=qq");
        startWeiboResponse.close();
        String weiboCookie = OkHttpUtils.getCookie(startWeiboResponse);
        String code = BotUtils.regex("crossidccode=", ";", weiboCookie);
        String startUrl = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=101019034&daid=383&pt_skey_valid=0&style=35&s_url=http%3A%2F%2Fconnect.qq.com&refer_cgi=authorize&which=&client_id=101019034&response_type=code&scope=get_info%2Cget_user_info&redirect_uri=https%3A%2F%2Fpassport.weibo.com%2Fothersitebind%2Fbind%3Fsite%3Dqq%26state%3D" + code + "%26bentry%3Dminiblog%26wl%3D&display=";
        Response startResponse = OkHttpUtils.get(startUrl);
        startResponse.close();
        String cookie = OkHttpUtils.getCookie(startResponse);
        String str = OkHttpUtils.getStr("https://ssl.ptlogin2.qq.com/pt_open_login?openlogin_data=which%3D%26refer_cgi%3Dauthorize%26response_type%3Dcode%26client_id%3D101019034%26state%3D%26display%3D%26openapi%3D%2523%26switch%3D0%26src%3D1%26sdkv%3D%26sdkp%3Da%26tid%3D1597734121%26pf%3D%26need_pay%3D0%26browser%3D0%26browser_error%3D%26serial%3D%26token_key%3D%26redirect_uri%3Dhttps%253A%252F%252Fpassport.weibo.com%252Fothersitebind%252Fbind%253Fsite%253Dqq%2526state%253D" + code + "%2526bentry%253Dminiblog%2526wl%253D%26sign%3D%26time%3D%26status_version%3D%26status_os%3D%26status_machine%3D%26page_type%3D1%26has_auth%3D0%26update_auth%3D0%26auth_time%3D" + new Date().getTime() + "&auth_token=" + QQUtils.getToken2(qqLoginEntity.getSuperToken()) + "&pt_vcode_v1=0&pt_verifysession_v1=&verifycode=&u=" + qqLoginEntity.getQq() + "&pt_randsalt=0&ptlang=2052&low_login_enable=0&u1=http%3A%2F%2Fconnect.qq.com&from_ui=1&fp=loginerroralert&device=2&aid=716027609&daid=383&pt_3rd_aid=101019034&ptredirect=1&h=1&g=1&pt_uistyle=35&regmaster=&",
                OkHttpUtils.addHeaders(qqLoginEntity.getCookieWithSuper() + cookie, startUrl));
        Result<String> result = QQUtils.getResultUrl(str);
        String url = result.getData();
        if (url == null) return Result.failure(result.getMessage(), null);
        Response secondResponse = OkHttpUtils.get(url,
                OkHttpUtils.addHeaders(weiboCookie, startUrl));
        secondResponse.close();
        String refererUrl = secondResponse.header("location");
        if (refererUrl == null) return Result.failure("登录失败，请稍后再试！！", null);
        Response thirdResponse = OkHttpUtils.get(refererUrl);
        String thirdHtml = OkHttpUtils.getStr(thirdResponse);
        String cnCookie = OkHttpUtils.getCookie(thirdResponse);
        String secondUrl = BotUtils.regex("location.replace\\(\"", "\"\\);", thirdHtml);
        if (secondUrl == null) return Result.failure("登录失败，请稍后再试！！", null);
        return Result.success(loginSuccess(cnCookie, refererUrl, secondUrl));
    }

    @Override
    public Map<String, String> loginByQr1() throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp("https://login.sina.com.cn/sso/qrcode/image?entry=weibo&size=180&callback=STK_16010457545441",
                OkHttpUtils.addReferer("https://weibo.com/"));
        jsonObject = jsonObject.getJSONObject("data");
        Map<String, String> map = new HashMap<>();
        map.put("id", jsonObject.getString("qrid"));
        map.put("url", jsonObject.getString("image"));
        return map;
    }

    @Override
    public Result<WeiboEntity> loginByQr2(String id) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp("https://login.sina.com.cn/sso/qrcode/check?entry=weibo&qrid=" + id + "&callback=STK_16010457545443",
                OkHttpUtils.addReferer("https://weibo.com/"));
        switch (jsonObject.getInteger("retcode")){
            case 20000000:
                JSONObject dataJsonObject = jsonObject.getJSONObject("data");
                String alt = dataJsonObject.getString("alt");
                Response response = OkHttpUtils.get("https://login.sina.com.cn/sso/login.php?entry=weibo&returntype=TEXT&crossdomain=1&cdult=3&domain=weibo.com&alt=" + alt + "&savestate=30&callback=STK_160104719639113");
                String cookie = OkHttpUtils.getCookie(response);
                jsonObject = OkHttpUtils.getJsonp(response);
                JSONArray jsonArray = jsonObject.getJSONArray("crossDomainUrlList");
                String url = jsonArray.getString(3);
                Response finallyResponse = OkHttpUtils.get(url);
                finallyResponse.close();
                String pcCookie = OkHttpUtils.getCookie(finallyResponse);
                String mobileCookie = getMobileCookie(cookie);
                return Result.success(new WeiboEntity(pcCookie, mobileCookie));
            case 50114001: return Result.failure(201, "未扫码！！");
            case 50114003: return Result.failure("您的微博登录二维码已失效！！", null);
            case 50114002: return Result.failure(202, "已扫码！！");
            default: return Result.failure(jsonObject.getString("msg"), null);
        }
    }

    @Override
    public Result<List<WeiboPojo>> getFriendWeibo(WeiboEntity weiboEntity) throws IOException {
        String str = OkHttpUtils.getStr("https://m.weibo.cn/feed/friends?",
                OkHttpUtils.addCookie(weiboEntity.getMobileCookie()));
        if (!"".equals(str)){
            JSONArray jsonArray;
            try {
                jsonArray = JSON.parseObject(str).getJSONObject("data").getJSONArray("statuses");
            }catch (JSONException e){
                return Result.failure("查询微博失败，请稍后再试！！", null);
            }
            List<WeiboPojo> list = new ArrayList<>();
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                WeiboPojo weiboPojo = convert(jsonObject);
                list.add(weiboPojo);
            }
            return Result.success(list);
        }else return Result.failure("您的cookie已失效，请重新绑定微博！！", null);
    }

    @Override
    public Result<List<WeiboPojo>> getMyWeibo(WeiboEntity weiboEntity) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://m.weibo.cn/profile/info",
                OkHttpUtils.addCookie(weiboEntity.getMobileCookie()));
        if (jsonObject.getInteger("ok") == 1){
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("statuses");
            List<WeiboPojo> list = new ArrayList<>();
            for (Object obj : jsonArray) {
                JSONObject singleJsonObject = (JSONObject) obj;
                list.add(convert(singleJsonObject));
            }
            if (list.size() == 0) return Result.failure("没有发现微博！！", null);
            else return Result.success(list);
        }else return Result.failure(jsonObject.getString("msg"), null);
    }

    @Override
    public Result<List<WeiboPojo>> weiboTopic(String keyword) throws IOException {
        Response response = OkHttpUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D1%26q%3D%23" + URLEncoder.encode(keyword, "utf-8") + "%23&page_type=searchall");
        if (response.code() != 200) return Result.failure("查询失败，请稍后再试！！", null);
        JSONObject jsonObject = OkHttpUtils.getJson(response);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("cards");
        List<WeiboPojo> list = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject singleJsonObject = (JSONObject) obj;
            JSONObject mBlogJsonObject = singleJsonObject.getJSONObject("mblog");
            if (mBlogJsonObject != null) list.add(convert(mBlogJsonObject));
        }
        if (list.size() == 0) return Result.failure("没有找到该话题", null);
        else return Result.success(list);
    }

    Result<WeiboToken> getToken(WeiboEntity weiboEntity) throws IOException {
        Response response = OkHttpUtils.get("https://m.weibo.cn/api/config",
                OkHttpUtils.addCookie(weiboEntity.getMobileCookie()));
        JSONObject jsonObject = OkHttpUtils.getJson(response).getJSONObject("data");
        if (jsonObject.getBoolean("login")){
            String cookie = OkHttpUtils.getCookie(response);
            return Result.success(new WeiboToken(jsonObject.getString("st"),
                    cookie + weiboEntity.getMobileCookie()));
        }else return Result.failure("登录已失效，", null);
    }

    @Override
    public String like(WeiboEntity weiboEntity, String id) throws IOException {
        WeiboToken weiboToken = getToken(weiboEntity).getData();
        if (weiboToken == null) return "登录已失效";
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("attitude", "heart");
        map.put("st", weiboToken.getToken());
        map.put("_spr", "screen:1536x864");
        JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/attitudes/create",
                map, OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/detail/" + id));
        return jsonObject.getString("msg");
    }

    @Override
    public String comment(WeiboEntity weiboEntity, String id, String commentContent) throws IOException {
        WeiboToken weiboToken = getToken(weiboEntity).getData();
        if (weiboToken == null) return "登录已失效！！";
        Map<String, String> map = new HashMap<>();
        map.put("content", commentContent);
        map.put("mid", id);
        map.put("st", weiboToken.getToken());
        map.put("_spr", "screen:411x731");
        JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/comments/create",
                map, OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/detail/" + id));
        if (jsonObject.getInteger("ok") == 1) return "评论成功！！";
        else return jsonObject.getString("msg");
    }

    private String uploadPic(String picUrl, String referer, WeiboToken weiboToken) throws IOException {
        MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("type", "json")
                .addFormDataPart("pic", "pic.jpg", OkHttpUtils.addStream(picUrl))
                .addFormDataPart("st", weiboToken.getToken())
                .addFormDataPart("_spr", "screen:411x731").build();
        JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/statuses/uploadPic",
                body, OkHttpUtils.addHeaders(weiboToken.getCookie(), referer));
        return jsonObject.getString("pic_id");
    }

    @Override
    public String forward(WeiboEntity weiboEntity, String id, String content, String picUrl) throws IOException {
        WeiboToken weiboToken = getToken(weiboEntity).getData();
        if (weiboToken == null) return "登录已失效";
        String picId = null;
        if (picUrl != null){
            picId = uploadPic(picUrl, "https://m.weibo.cn/compose/repost?id=" + id, weiboToken);
        }
        FormBody.Builder builder = new FormBody.Builder()
                .add("id", id)
                .add("content", content)
                .add("mid", id)
                .add("st", weiboToken.getToken())
                .add("_spr", "screen:411x731");
        if (picId != null) builder.add("picId", picId);
        else picId = "";
        JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/statuses/repost", builder.build(),
                OkHttpUtils.addHeaders(weiboToken.getCookie(),
                        "https://m.weibo.cn/compose/repost?id=" + id + "&pids=" + picId));
        if (jsonObject.getInteger("ok") == 1) return "转发微博成功！！";
        else return jsonObject.getString("msg");
    }

    @Override
    public String getUserInfo(String id) throws IOException {
        Response response = OkHttpUtils.get("https://m.weibo.cn/api/container/getIndex?uid=" + id + "&luicode=10000011&lfid=100103type%3D1&containerid=100505" + id);
        if (response.code() == 200){
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            JSONObject userInfoJsonObject = jsonObject.getJSONObject("data").getJSONObject("userInfo");
            return "id：" + userInfoJsonObject.getString("id") + "\n" +
                    "昵称：" + userInfoJsonObject.getString("screen_name") + "\n" +
                    "关注：" + userInfoJsonObject.getString("follow_count") + "\n" +
                    "粉丝：" + userInfoJsonObject.getString("followers_count") + "\n" +
                    "微博会员：" + userInfoJsonObject.getString("mbrank") + "级\n" +
                    "微博认证：" + userInfoJsonObject.getString("verified_reason") + "\n" +
                    "描述：" + userInfoJsonObject.getString("description") + "\n" +
                    "主页：" + "https://m.weibo.cn/u/" + userInfoJsonObject.getString("id");
        }else return "查询失败，请稍后再试！！！";
    }

    @Override
    public String publishWeibo(WeiboEntity weiboEntity, String content, List<String> url) throws IOException {
        WeiboToken weiboToken = getToken(weiboEntity).getData();
        if (weiboToken == null) return "登录已失效";
        StringBuilder picIds = new StringBuilder();
        if (url != null){
            url.forEach(str -> {
                try {
                    String id = uploadPic(str, "https://m.weibo.cn/compose/", weiboToken);
                    picIds.append(id).append(",");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        FormBody.Builder builder = new FormBody.Builder()
                .add("content", content)
                .add("st", weiboToken.getToken())
                .add("_spr", "screen:411x731");
        StringBuilder newSb = picIds.deleteCharAt(picIds.length() - 1);
        if (picIds.length() != 0){
            builder.add("picId", newSb.toString());
        }
        JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/statuses/update", builder.build(),
                OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/compose/?pids=" + newSb));
        if (jsonObject.getInteger("ok") == 1) return "发布微博成功！！";
        else return jsonObject.getString("msg");
    }

    @Override
    public String removeWeibo(WeiboEntity weiboEntity, String id) throws IOException {
        WeiboToken weiboToken = getToken(weiboEntity).getData();
        if (weiboToken == null) return "登录已失效";
        Map<String, String> map = new HashMap<>();
        map.put("mid", id);
        map.put("st", weiboToken.getToken());
        map.put("_spr", "screen:411x731");
        JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/profile/delMyblog",
                map, OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/profile/"));
        if (jsonObject.getInteger("ok") == 1) return "删除微博成功！！";
        else return jsonObject.getString("msg");
    }

    @Override
    public String favoritesWeibo(WeiboEntity weiboEntity, String id) throws IOException {
        WeiboToken weiboToken = getToken(weiboEntity).getData();
        if (weiboToken == null) return "登录已失效";
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("st", weiboToken.getToken());
        map.put("_spr", "screen:411x731");
        JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/mblogDeal/addFavMblog",
                map, OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/"));
        return jsonObject.getString("msg");
    }

    @Override
    public String shortUrl(WeiboEntity weiboEntity, String url) throws IOException {
        WeiboToken weiboToken = getToken(weiboEntity).getData();
        if (weiboToken == null) return "登录已失效";
        Map<String, String> map = new HashMap<>();
        if (!url.startsWith("http")) url = "http://" + url;
        map.put("content", url);
        map.put("st", weiboToken.getToken());
        map.put("_spr", "screen:1536x864");
        JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/statuses/update", map,
                OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/compose/"));
        if (jsonObject.getInteger("ok") == 1){
            JSONObject dataJsonObject = jsonObject.getJSONObject("data");
            String id = dataJsonObject.getString("id");
            String content = dataJsonObject.getString("text");
            String shortUrl = Jsoup.parse(content).getElementsByTag("a").first().attr("href");
            removeWeibo(weiboEntity, id);
            return shortUrl;
        }else return "获取短链接失败！！";
    }
}
