package me.kuku.yuq.utils;

import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.QQLoginService;
import okhttp3.Response;
import org.jsoup.internal.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class QQUtils {
    public static String getGTK2(String sKey) {
        long salt = 5381;
        String md5key = "tencentQQVIP123443safde&!%^%1282";
        List<Long> hash = new ArrayList<>();
        hash.add(salt << 5);
        int len = sKey.length();
        for (int i = 0; i < len; i++){
            String ASCIICode = Integer.toHexString(sKey.charAt(i));
            long code = Integer.valueOf(ASCIICode, 16);
            hash.add((salt << 5) + code);
            salt = code;
        }
        String md5str = StringUtil.join(hash, "") + md5key;
        md5str = MD5Utils.toMD5(md5str);
        return md5str;
    }

    public static long getGTK(String psKey){
        int len = psKey.length();
        long hash = 5381L;
        for (int i = 0; i < len; i++){
            hash += (hash << 5 & 2147483647) + (int) psKey.charAt(i) & 2147483647;
            hash &= 2147483647;
        }
        return hash & 2147483647;
    }

    public static Long getToken(String token){
        int len = token.length();
        long hash = 0L;
        for (int i = 0; i < len; i++){
            hash = (hash * 33 + (int) token.charAt(i)) % 4294967296L;
        }
        return hash;
    }

    public static Long getToken2(String token){
        int len = token.length();
        long hash = 0L;
        for (int i = 0; i < len; i++){
            hash += (hash << 5) + (Integer.parseInt(String.valueOf(token.charAt(i))) & 2147483647);
            hash = hash & 2147483647;
        }
        return hash & 2147483647;
    }

    public static Result<String> getResultUrl(String str){
        String ss = BotUtils.regex("'", "','", str);
        String msg = null;
        if (ss == null) {
            msg = BotUtils.regex(",'0','", "', ' '", str);
            if (msg == null) msg = "其他错误";
        }
        if (msg == null) {
            int num = Integer.parseInt(ss);
            switch (num) {
                case 4:
                    msg = "验证码错误，登录失败！！";
                    break;
                case 3:
                    msg = "密码错误，登录失败！！";
                    break;
                case 19:
                    msg = "您的QQ号已被冻结，登录失败！";
                    break;
                case 10009:
                    return Result.failure(10009, "您的QQ号登录需要验证短信，请输入短信验证码！！");
                case 0:
                case 2: {
                    String url = BotUtils.regex(",'0','", "','", str);
                    if (url == null) url = BotUtils.regex("','", "'", str);
                    if (url != null) return Result.success(url);
                    else msg = "";
                    break;
                }
                case 1:
                case -1:
                case 7:
                    msg = "superKey已失效，请更新QQ！";
                    break;
                default:
                    msg = "其他错误";
            }
        }
        if (msg.contains("superKey")) return Result.failure(502, msg);
        return Result.failure(500, msg);
    }

    public static Result<String> getPtToken(String str){
        Result<String> result = getResultUrl(str);
        if (result.getCode() == 200){
            String url = result.getData();
            String token = BotUtils.regex("ptsigx=", "&", url);
            return Result.success(token);
        }else return result;
    }

    public static Map<String, String> getKey(String url) throws IOException {
        Response response = OkHttpUtils.get(url);
        response.close();
        String cookie = OkHttpUtils.getCookie(response);
        Map<String, String> map = new HashMap<>();
        map.put("p_skey", OkHttpUtils.getCookie(cookie, "p_skey"));
        map.put("pt4_token", OkHttpUtils.getCookie(cookie, "pt4_token"));
        return map;
    }

    public static Map<String, String> getKey(String pt, String qq, String domain, String suffixUrl) throws IOException {
        return getKey(String.format("https://%s/check_sig?uin=%s&ptsigx=%s%s", domain, qq, pt, suffixUrl));
    }

    public static Result<Map<String ,String>> qrCodeLoginVerify(String sig, String appId, String daId, String url) throws IOException {
        Result<Map<String, String>> result;
        do {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!"".equals(appId)) result = QQQrCodeLoginUtils.checkQrCode(appId, daId, url, sig);
            else result = QQQrCodeLoginUtils.checkQrCode(sig);
        }while (result.getCode() == 0);
        return result;
    }

    public static Result<Map<String ,String>> qrCodeLoginVerify(String sig) throws IOException {
        return qrCodeLoginVerify(sig, "", "", "");
    }

    public static QQLoginEntity convertQQEntity(Map<String, String> map, QQLoginEntity qqLoginEntity){
        if (qqLoginEntity == null) qqLoginEntity = new QQLoginEntity();
        qqLoginEntity.setSKey(map.get("skey"));
        qqLoginEntity.setPsKey(map.get("p_skey"));
        qqLoginEntity.setSuperKey(map.get("superkey"));
        qqLoginEntity.setSuperToken(map.get("supertoken"));
        qqLoginEntity.setPt4Token(map.get("pt4_token"));
        qqLoginEntity.setStatus(true);
        return qqLoginEntity;
    }

    public static void saveOrUpdate(QQLoginService qqLoginService, Map<String, String> map, long qq, String password, Long group){
        QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qq);
        if (qqLoginEntity == null) qqLoginEntity = new QQLoginEntity();
        qqLoginEntity = convertQQEntity(map, qqLoginEntity);
        qqLoginEntity.setQq(qq);
        if (group != null) qqLoginEntity.setGroup(group);
        if (password != null) qqLoginEntity.setPassword(password);
        qqLoginService.save(qqLoginEntity);
    }
}
