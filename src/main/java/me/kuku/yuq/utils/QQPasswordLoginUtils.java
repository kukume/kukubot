package me.kuku.yuq.utils;

import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import okhttp3.Response;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class QQPasswordLoginUtils {

    private static String encryptPassword(Long qq, String password, String vCode) throws IOException {
        ScriptEngine se = new ScriptEngineManager().getEngineByName("JavaScript");
        try {
            se.eval(OkHttpUtils.getStr("https://u.iheit.com/kuku/login.js"));
            return se.eval("getmd5('" + qq + "','" + password + "','" + vCode +"')").toString();
        } catch (ScriptException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static Map<String, String> checkVc(String appId, String daId, String url, String qq) throws IOException {
        Response firstResponse = OkHttpUtils.get("https://xui.ptlogin2.qq.com/cgi-bin/xlogin?proxy_url=https%3A//qzs.qq.com/qzone/v6/portal/proxy.html&daid=" + daId + "&&hide_title_bar=1&low_login=0&qlogin_auto_login=1&no_verifyimg=1&link_target=blank&appid=" + appId + "&style=22&target=self&s_url=" + URLEncoder.encode(url, "utf-8") + "&pt_qr_app=%E6%89%8B%E6%9C%BAQQ%E7%A9%BA%E9%97%B4&pt_qr_link=http%3A//z.qzone.com/download.html&self_regurl=https%3A//qzs.qq.com/qzone/v6/reg/index.html&pt_qr_help_link=http%3A//z.qzone.com/download.html&pt_no_auth=1",
                OkHttpUtils.addUA(UA.PC));
        firstResponse.close();
        String cookie = OkHttpUtils.getCookie(firstResponse);
        String loginSig = BotUtils.regex("pt_login_sig=", "; ", cookie);
        Response secondResponse = OkHttpUtils.get(String.format("https://ssl.ptlogin2.qq.com/check?regmaster=&pt_tea=2&pt_vcode=1&uin=%s&appid=%s&js_ver=20102616&js_type=1&login_sig=%s&u1=%s&r=0.%s&pt_uistyle=40",
                qq, appId, loginSig, URLEncoder.encode(url, "utf-8"), BotUtils.randomNum(16)),
                OkHttpUtils.addHeaders(cookie, "https://xui.ptlogin2.qq.com", UA.PC));
        String resultStr = OkHttpUtils.getStr(secondResponse);
        cookie += OkHttpUtils.getCookie(secondResponse);
        String[] arr = BotUtils.regex("\\('", "\\)", resultStr).split("','");
        Map<String, String> map = new HashMap<>();
        if ("0".equals(arr[0])){
            map.put("code", arr[0]);
            map.put("randStr", arr[1]);
            map.put("ticket", arr[3]);
            map.put("cookie", cookie);
        }else {
            map.put("code", arr[0]);
            map.put("sig", arr[1]);
            map.put("cookie", cookie);
        }
        String sid = arr[6];
        map.put("sid", sid.substring(0, sid.length() - 1));
        map.put("loginSig", loginSig);
        map.put("ptdrvs", BotUtils.regex("(?<=ptdrvs=).+?(?=;)", cookie));
        return map;
    }

    private static Result<Map<String, String>> login(String appId, String daId, Long qq, String password, String url, Map<String, String> map) throws IOException {
        String randStr = map.get("randStr");
        String cookie = map.get("cookie");
        String smsCode = map.get("smsCode");
        String encryptPassword = encryptPassword(qq, password, randStr);
        int v1 = 1;
        if (randStr.startsWith("!")) v1 = 0;
        String uri = String.format("https://ssl.ptlogin2.qq.com/login?u=%s&verifycode=%s&pt_vcode_v1=%s&pt_verifysession_v1=%s&p=%s&pt_randsalt=2&u1=%s&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=1-1-1606018294463&js_ver=20102616&js_type=1&login_sig=%s&pt_uistyle=40&aid=%s&daid=%s&ptdrvs=%s&sid=%s&",
                qq, randStr, v1, map.get("ticket"), encryptPassword, URLEncoder.encode(url, "utf-8"), map.get("loginSig"), appId, daId, map.get("ptdRvs"), map.get("sid"));
        if (smsCode != null){
            uri += "&pt_sms_code=" + smsCode;
            cookie += "pt_sms=" + smsCode;
        }
        Response response = OkHttpUtils.get(uri, OkHttpUtils.addHeaders(cookie, "https://xui.ptlogin2.qq.com/", UA.PC));
        String resultCookie = OkHttpUtils.getCookie(response);
        Map<String, String> cookieMap = new HashMap<>();
        cookieMap.put("skey", OkHttpUtils.getCookie(resultCookie, "skey"));
        cookieMap.put("superkey", OkHttpUtils.getCookie(resultCookie, "superkey"));
        cookieMap.put("supertoken", OkHttpUtils.getCookie(resultCookie, "supertoken"));
        String str = OkHttpUtils.getStr(response);
        Result<String> result = QQUtils.getResultUrl(str);
        switch (result.getCode()){
            case 200:
                Map<String, String> otherKeys = QQUtils.getKey(result.getData());
                cookieMap.putAll(otherKeys);
                return Result.success(cookieMap);
            case 10009:
                Map<String, String> newCookieMap = OkHttpUtils.getCookie(cookie, "ptdrvs", "pt_sms_ticket");
                String smsCookie = cookie.replaceAll(map.get("ptdRvs"), newCookieMap.get("ptdrvs") + "; pt_sms_ticket=" + newCookieMap.get("pt_sms_ticket"));
                sendSms(appId, qq, newCookieMap.get("pt_sms_ticket"), smsCookie);
                Map<String, String> smsMap = new HashMap<>();
                smsMap.put("randStr", randStr);
                smsMap.put("ticket", map.get("ticket"));
                smsMap.put("cookie", smsCookie);
                smsMap.put("sid", map.get("sid"));
                return Result.failure(10009, result.getMessage(), smsMap);
            case 502: return Result.failure("登录失败，请稍后再试！", null);
            default: return Result.failure(result.getMessage(), null);
        }
    }

    private static void sendSms(String appId, Long qq, String smsTicket, String cookie) throws IOException {
        OkHttpUtils.get(String.format("https://ssl.ptlogin2.qq.com/send_sms_code?bkn=&uin=%s&aid=%s&pt_sms_ticket=%s",
                qq, appId, smsTicket), OkHttpUtils.addCookie(cookie)).close();
    }

    public static Result<Map<String, String>> login(String appId, String daId, Long qq, String password, String url) throws IOException {
        Map<String, String> map = checkVc(appId, daId, url, qq.toString());
        if (!"0".equals(map.get("code"))){
            Result<Map<String, String>> result = TenCentCaptchaUtils.identify(appId, map.get("loginSig"), qq);
            if (result.getCode() == 200) map.putAll(result.getData());
            else return result;
        }
        return login(appId, daId, qq, password, url, map);
    }

    public static Result<Map<String, String>> login(Long qq, String password) throws IOException {
        return login("549000912", "5", qq, password, "https://qzs.qzone.qq.com/qzone/v5/loginsucc.html?para=izone");
    }

    public static Result<Map<String, String>> loginBySms(String appId, String daId, Long qq, String password, String url, Map<String, String> map) throws IOException {
        return login(appId, daId, qq, password, url, map);
    }

    public static Result<Map<String, String>> loginBySms(Long qq, String password, Map<String, String> map) throws IOException {
        return login("549000912", "5", qq, password, "https://qzs.qzone.qq.com/qzone/v5/loginsucc.html?para=izone", map);
    }
}
