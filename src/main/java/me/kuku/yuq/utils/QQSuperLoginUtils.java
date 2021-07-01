package me.kuku.yuq.utils;

import me.kuku.pojo.Result;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.QqUtils;
import me.kuku.yuq.entity.QQLoginEntity;

import java.io.IOException;
import java.util.Map;

public class QQSuperLoginUtils {
    private static Result<String> login(QQLoginEntity qqLoginEntity, String appId, String daId, String prefixUrl, String suffixUrl) throws IOException {
        String str = OkHttpUtils.getStr(String.format("https://ssl.ptlogin2.qq.com/pt4_auth?daid=%s&appid=%s&auth_token=%s", daId, appId, qqLoginEntity.getToken()),
                OkHttpUtils.addHeaders(qqLoginEntity.getCookieWithSuper(), "https://ui.ptlogin2.qq.com/cgi-bin/login"));
        Result<String> result = QqUtils.getPtToken(str);
        if (result.getCode() == 200){
            Map<String, String> map = QqUtils.getKey(result.getData(), qqLoginEntity.getQq().toString(), prefixUrl, suffixUrl);
            return Result.success(map.get("p_skey"));
        }else return Result.failure(result.getMessage(), null);
    }

    public static Result<String> vipLogin(QQLoginEntity qqLoginEntity) throws IOException {
        return login(qqLoginEntity, "8000212", "18", "ptlogin2.vip.qq.com", "&daid=18&pt_login_type=4&service=pt4_auth&pttype=2&regmaster=&aid=8000212&s_url=https%3A%2F%2Fzb.vip.qq.com%2Fsonic%2Fbubble");
    }

    public static Result<String> blueLogin(QQLoginEntity qqLoginEntity) throws IOException {
        return login(qqLoginEntity, "21000110", "176", "ptlogin2.gamevip.qq.com", "&daid=176&pt_login_type=4&service=pt4_auth&pttype=2&regmaster=&aid=21000110&s_url=http%3A%2F%2Fgamevip.qq.com%2F");
    }

    public static Result<String> weiYunLogin(QQLoginEntity qqLoginEntity) throws IOException {
        return login(qqLoginEntity, "527020901", "372", "ssl.ptlogin2.weiyun.com", "&s_url=https%3A%2F%2Fh5.weiyun.com%2Fsign_in&f_url=&ptlang=2052&ptredirect=101&aid=527020901&daid=372&j_later=0&low_login_hour=720&regmaster=0&pt_login_type=1&pt_aid=0&pt_aaid=0&pt_light=0&pt_3rd_aid=0&service=login&nodirect=0");
    }
}
