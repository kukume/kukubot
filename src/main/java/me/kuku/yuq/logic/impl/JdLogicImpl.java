package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import me.kuku.pojo.*;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.QqQrCodeLoginUtils;
import me.kuku.yuq.entity.QlEntity;
import me.kuku.yuq.logic.JdLogic;
import me.kuku.yuq.pojo.JdQrcode;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JdLogicImpl implements JdLogic {

    private final QqApp qqApp = new QqApp(716027609L, 383, 100273020L);

    private Token cache = null;

    @AllArgsConstructor
    private static class Token{
        private String token;
        private Long expiration;

        private boolean isExpire(){
            return System.currentTimeMillis() > expiration;
        }
    }

    private String refererUrl(long time){
        return "https://plogin.m.jd.com/login/login?appid=300&returnurl=https://wqlogin2.jd.com/passport/LoginRedirect?state=" +
                time + "&returnurl=//home.m.jd.com/myJd/newhome.action?sceneval=2&ufc=&/myJd/home.action&source=wq_passport";
    }

    @Override
    public JdQrcode qrcode() throws IOException {
        Response response = OkHttpUtils.get("https://plogin.m.jd.com/cgi-bin/m/qqlogin?appid=300&returnurl=https%3A%2F%2Fwq.jd.com%2Fpassport%2FLoginRedirect%3Fstate%3D1906665712%26returnurl%3Dhttps%253A%252F%252Fhome.m.jd.com%252FmyJd%252Fnewhome.action%253Fsceneval%253D2%2526ufc%253D%2526&source=wq_passport");
        response.close();
        String cookie = OkHttpUtils.getCookie(response);
        String lSid = OkHttpUtils.getCookie(cookie, "lsid");
        String location = response.header("location");
        String state = MyUtils.regex("(?<=state=).*", location);
        String url = MyUtils.regex("redirect_uri=", "&", location);
        String redirectUrl = URLDecoder.decode(url, "utf-8");
        QqLoginQrcode qrcode = QqQrCodeLoginUtils.getQrcode(qqApp);
        return new JdQrcode(qrcode, cookie, state, redirectUrl, lSid);
    }

    @Override
    public Result<String> cookie(JdQrcode jdQrcode) throws IOException {
        Result<String> res = QqQrCodeLoginUtils.authorize(qqApp, jdQrcode.getQqLoginQrcode().getSig(), jdQrcode.getState(), jdQrcode.getRedirectUrl());
        if (res.isFailure()) return res;
        String url = res.getData();
        Response response = null;
        do {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = OkHttpUtils.get(url);
            response.close();
        } while (response.code() != 302);
        // https://wq.jd.com/passport/LoginRedirect
        String notUrl = response.header("location");
        if (notUrl.contains("https://plogin.m.jd.com/h5/risk/select?")){
            return Result.failure("请先前往" + notUrl + "进行手机验证，再重新使用QQ扫码登录！");
        }
        if (notUrl.contains("https://plogin.m.jd.com/joinlogin/bind")){
            return Result.failure("您的qq号未绑定jd，请先前往" + notUrl + "绑定您的jd账号");
        }
        if (!notUrl.contains("https://wq.jd.com/passport/LoginRedirect")){
            return Result.failure("未知原因获取cookie失败，请前往" + notUrl + "查看原因！");
        }
        String cookie = OkHttpUtils.getCookie(response);
        String ptKeyCookie = OkHttpUtils.getCookieStr(response, "pt_key");
        String ptPinCookie = OkHttpUtils.getCookieStr(response, "pt_pin");
        String resCookie = ptKeyCookie.trim() + ptPinCookie.trim();
        OkHttpUtils.get(notUrl, OkHttpUtils.addHeaders(cookie + jdQrcode.getTempCookie(), "https://graph.qq.com/",
                UA.MOBILE)).close();
        return Result.success(resCookie);
    }

    @Override
    public Result<?> qlCookie(QlEntity qlEntity, String cookie) throws IOException {
        String url = qlEntity.getUrl();
        String auth;
        if (cache == null || cache.isExpire()){
            JSONObject loginResultJsonObject = OkHttpUtils.getJson(url + "/open/auth/token?client_id=" +
                    qlEntity.getClientId() + "&client_secret=" + qlEntity.getClientSecret());
            if (loginResultJsonObject.getInteger("code") == 200){
                JSONObject dataJsonObject = loginResultJsonObject.getJSONObject("data");
                auth = "Bearer " + dataJsonObject.getString("token");
                cache = new Token(auth, dataJsonObject.getLong("expiration"));
            }else return Result.failure(loginResultJsonObject.getString("message"));
        }else {
            auth = cache.token;
        }
        String updateUrl = url + "/open/envs?t=" + System.currentTimeMillis();
        Headers headers = OkHttpUtils.addSingleHeader("Authorization", auth);
        JSONObject searchJsonObject = OkHttpUtils.getJson(url + "/open/envs?searchValue=&t=" + System.currentTimeMillis(),
                headers);
        List<JSONObject> cookieList = searchJsonObject.getJSONArray("data")
                .stream().map(it -> (JSONObject) it).filter(it -> "JD_COOKIE".equals(it.getString("name")))
                .collect(Collectors.toList());
        JSONObject params = new JSONObject();
        params.put("name", "JD_COOKIE");
        params.put("value", cookie);
        boolean isAdd = true;
        for (JSONObject jsonObject : cookieList) {
            String value = jsonObject.getString("value");
            String pin = MyUtils.regex("pt_pin=", ";", value);
            String nowPin = MyUtils.regex("pt_pin=", ";", cookie);
            if (pin.equals(nowPin)){
                params.put("_id", jsonObject.getString("_id"));
                isAdd = false;
                break;
            }
        }
        Response response;
        if (isAdd){
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(params);
            response = OkHttpUtils.post(updateUrl, OkHttpUtils.addJson(jsonArray.toJSONString()),
                    headers);
        }else
            response = OkHttpUtils.put(updateUrl, OkHttpUtils.addJson(params.toJSONString()),
                    headers);
        JSONObject jsonObject = OkHttpUtils.getJson(response);
        if (jsonObject.getInteger("code") == 200) {
            Object data = jsonObject.get("data");
            String resultId;
            if (data instanceof JSONObject)
                resultId = ((JSONObject) data).getString("_id");
            else if (data instanceof JSONArray)
                resultId = ((JSONArray) data).getJSONObject(0).getString("_id");
            else return Result.failure("添加失败，请重试！");
            return Result.success(Result.map("id", resultId));
        }
        else return Result.failure(jsonObject.getString("message"));
    }
}
