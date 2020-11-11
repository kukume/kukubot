package me.kuku.yuq.utils;

import me.kuku.yuq.pojo.Result;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class QQQrCodeLoginUtils {

    public static Map<String, Object> getQrCode(String appId, String daId) throws IOException {
        Response response = OkHttpUtils.get(String.format("https://ssl.ptlogin2.qq.com/ptqrshow?appid=%s&e=2&l=M&s=3&d=72&v=4&t=0.%s&daid=%s&pt_3rd_aid=0",
                appId, BotUtils.randomStr(17), daId));
        byte[] bytes = OkHttpUtils.getBytes(response);
        String cookie = OkHttpUtils.getCookie(response);
        String sig = OkHttpUtils.getCookie(cookie, "qrsig");
        Map<String, Object> map = new HashMap<>();
        map.put("qrCode", bytes);
        map.put("sig", sig);
        return map;
    }

    public static Map<String, Object> getQrCode() throws IOException {
        return getQrCode("549000912", "5");
    }

    public static Result<Map<String, String>> checkQrCode(String appId, String daId, String url, String sig) throws IOException {
        Response response = OkHttpUtils.get(String.format("https://ssl.ptlogin2.qq.com/ptqrlogin?u1=%s&ptqrtoken=%s&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=0-0-1591074900575&js_ver=20032614&js_type=1&login_sig=&pt_uistyle=40&aid=%s&daid=%s&",
                URLEncoder.encode(url, "utf-8"), getPtGrToken(sig), appId, daId), OkHttpUtils.addCookie("qrsig=" + sig));
        String str = OkHttpUtils.getStr(response);
        switch (Integer.parseInt(BotUtils.regex("'", "','", str))){
            case 0:
                String cookie = OkHttpUtils.getCookie(response);
                Map<String, String> cookieMap = OkHttpUtils.getCookie(cookie, "skey", "superkey", "supertoken");
                Result<String> result = QQUtils.getResultUrl(str);
                Map<String, String> map = QQUtils.getKey(result.getData());
                cookieMap.putAll(map);
                return Result.success(cookieMap);
            case 66:
            case 67:
                return Result.failure(0, "未失效或者验证中！");
            default: return Result.failure(BotUtils.regex("','','0','", "', ''", str), null);
        }
    }

    public static Result<Map<String, String>> checkQrCode(String sig) throws IOException {
        return checkQrCode("549000912", "5", "https://qzs.qzone.qq.com/qzone/v5/loginsucc.html?para=izone", sig);
    }

    private static int getPtGrToken(String sig) {
        int e = 0;
        for (int i = 0, n = sig.length(); n > i; ++i)
            e += (e << 5) + (int) sig.charAt(i);
        return e & 2147483647;
    }

}
