package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.MotionEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.LeXinMotionLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import me.kuku.yuq.utils.QQUtils;
import okhttp3.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LeXinMotionLogicImpl implements LeXinMotionLogic {
    @Override
    public byte[] getCaptchaImage(String phone) throws IOException {
        return OkHttpUtils.getBytes("https://sports.lifesense.com/sms_service/verify/getValidateCode?requestId=1000&sessionId=nosession&mobile=" + phone);
    }

    @Override
    public Result<String> getCaptchaCode(String phone, String captchaImageCode) throws IOException {
        JSONObject jsonObject = OkHttpUtils.postJson("https://sports.lifesense.com/sms_service/verify/sendCodeWithOptionalValidate?requestId=1000&sessionId=nosession",
                OkHttpUtils.addJson(String.format("{\"code\":\"%s\",\"mobile\":\"%s\"}", captchaImageCode, phone)));
        switch (jsonObject.getInteger("code")){
            case 200: return Result.failure(200, "验证码发送成功，请输入验证码");
            case 416: return Result.failure(416, "验证码已失效！！");
            case 412: return Result.failure(412, "验证码错误！！");
            default: return Result.failure(jsonObject.getString("msg"), null);
        }
    }

    @Override
    public Result<MotionEntity> loginByPassword(String phone, String password) throws IOException {
        Response response = OkHttpUtils.post("https://sports.lifesense.com/sessions_service/login?screenHeight=2267&screenWidth=1080&systemType=2&version=4.5",
                OkHttpUtils.addJson(String.format("{\"password\":\"%s\",\"clientId\":\"%s\",\"appType\":6,\"loginName\":\"%s\",\"roleType\":0}", password, BotUtils.randomNum(32), phone)));
        JSONObject jsonObject = OkHttpUtils.getJson(response);
        if (jsonObject.getInteger("code").equals(200)){
            return Result.success(new MotionEntity(
                    phone, password, OkHttpUtils.getCookie(response), jsonObject.getJSONObject("data").getString("userId"),
                    jsonObject.getJSONObject("data").getString("accessToken")
            ));
        }else return Result.failure(jsonObject.getString("msg"), null);
    }

    @Override
    public Result<MotionEntity> loginByQQ(QQLoginEntity qqLoginEntity) throws IOException {
        Response firstResponse = OkHttpUtils.get("https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=1101774620&daid=381&pt_skey_valid=1&style=35&s_url=http://connect.qq.com&refer_cgi=m_authorize&ucheck=1&fall_to_wv=1&status_os=9.3.2&redirect_uri=auth://www.qq.com&client_id=1104904286&response_type=token&scope=all&sdkp=i&sdkv=2.9&state=test&status_machine=iPhone8,1&switch=1",
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithSuper()));
        firstResponse.close();
        String addCookie = OkHttpUtils.getCookie(firstResponse);
        String str = OkHttpUtils.getStr("https://ssl.ptlogin2.qq.com/pt_open_login?openlogin_data=appid%3D716027609%26pt_3rd_aid%3D1101774620%26daid%3D381%26pt_skey_valid%3D1%26style%3D35%26s_url%3Dhttp%3A%2F%2Fconnect.qq.com%26refer_cgi%3Dm_authorize%26ucheck%3D1%26fall_to_wv%3D1%26status_os%3D9.3.2%26redirect_uri%3Dauth%3A%2F%2Fwww.qq.com%26client_id%3D1104904286%26response_type%3Dtoken%26scope%3Dall%26sdkp%3Di%26sdkv%3D2.9%26state%3Dtest%26status_machine%3DiPhone8%2C1%26switch%3D1%26pt_flex%3D1&auth_token=" + QQUtils.getToken2(qqLoginEntity.getSuperToken()) + "&pt_vcode_v1=0&pt_verifysession_v1=&verifycode=&u=" + qqLoginEntity.getQq() + "&pt_randsalt=0&ptlang=2052&low_login_enable=0&u1=http%3A%2F%2Fconnect.qq.com&from_ui=1&fp=loginerroralert&device=2&aid=716027609&daid=381&pt_3rd_aid=1101774620&ptredirect=1&h=1&g=1&pt_uistyle=35&regmaster=&",
                OkHttpUtils.addHeaders(qqLoginEntity.getCookieWithSuper() + addCookie, "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=1101774620&daid=381&pt_skey_valid=1&style=35&s_url=http://connect.qq.com&refer_cgi=m_authorize&ucheck=1&fall_to_wv=1&status_os=9.3.2&redirect_uri=auth://www.qq.com&client_id=1104904286&response_type=token&scope=all&sdkp=i&sdkv=2.9&state=test&status_machine=iPhone8,1&switch=1", UA.PC));
        Result<String> result = QQUtils.getResultUrl(str);
        if (result.getCode() == 200){
            String url = result.getData();
            String openId = BotUtils.regex("openid=", "&", url);
            String accessToken = BotUtils.regex("access_token=", "&", url);
            Response response = OkHttpUtils.post("https://sports.lifesense.com/sessions_service/loginFromOpenId?systemType=2&version=3.7.5",
                    OkHttpUtils.addJson("{\"openAccountType\":2,\"clientId\":\"" + BotUtils.randomStr(33) + "}\",\"expireTime\":" + (new Date().getTime() + 1000L * 60 * 60 * 24 * 90) + ",\"appType\":6,\"openId\":\"" + openId + "\",\"roleType\":0,\"openAccessToken\":\"" + accessToken + "\"}"));
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            String cookie = OkHttpUtils.getCookie(response);
            if (jsonObject.getInteger("code") == 200){
                return Result.success(new MotionEntity(null, null, cookie, jsonObject.getJSONObject("data").getString("userId"),
                        jsonObject.getJSONObject("data").getString("accessToken")));
            }else return Result.failure("您没有使用qq绑定lexin运动，登录失败！！", null);
        }else return Result.failure("您的QQ已失效，请更新QQ！！", null);
    }

    @Override
    public Result<MotionEntity> loginByPhoneCaptcha(String phone, String captchaPhoneCode) throws IOException {
        Response response = OkHttpUtils.post("https://sports.lifesense.com/sessions_service/loginByAuth?screenHeight=2267&screenWidth=1080&systemType=2&version=4.5",
                OkHttpUtils.addJson(String.format("{\"clientId\": \"%s\",\"authCode\": \"%s\",\"appType\": \"6\",\"loginName\": \"%s\"}", BotUtils.randomStr(32), captchaPhoneCode, phone)));
        JSONObject jsonObject = OkHttpUtils.getJson(response);
        int code = jsonObject.getInteger("code");
        if (code == 200){
            return Result.success(new MotionEntity("", "", OkHttpUtils.getCookie(response),
                    jsonObject.getJSONObject("data").getString("userId"),
                    jsonObject.getJSONObject("data").getString("accessToken")));
        } else if (code == 412) return Result.failure(412, "验证码错误！！");
        else return Result.failure(jsonObject.getString("msg"), null);
    }

    @Override
    public String modifyStepCount(int step, MotionEntity motionEntity) throws IOException {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String tenTime = String.valueOf(date.getTime()).substring(0, 10);
        JSONObject jsonObject = OkHttpUtils.postJson("https://sports.lifesense.com/sport_service/sport/sport/uploadMobileStepV2?country=%E4%B8%AD%E5%9B%BD&city=%E6%9F%B3%E5%B7%9E&cityCode=450200&timezone=Asia%2FShanghai&latitude=24.368694&os_country=CN&channel=qq&language=zh&openudid=&platform=android&province=%E5%B9%BF%E8%A5%BF%E5%A3%AE%E6%97%8F%E8%87%AA%E6%B2%BB%E5%8C%BA&appType=6&requestId=" + BotUtils.randomStr(32) + "&countryCode=&systemType=2&longitude=109.532216&devicemodel=V1914A&area=CN&screenwidth=1080&os_langs=zh&provinceCode=450000&promotion_channel=qq&rnd=3d51742c&version=4.6.7&areaCode=450203&requestToken=" + BotUtils.randomStr(32) + "&network_type=wifi&osversion=10&screenheight=2267&ts=" + tenTime,
                OkHttpUtils.addJson(String.format("{\"list\":[{\"active\":1,\"calories\":%d,\"created\":\"%s\",\"dataSource\":2,\"dayMeasurementTime\":\"%s\",\"deviceId\":\"M_NULL\",\"distance\":%d,\"id\":\"%s\",\"isUpload\":0,\"measurementTime\":\"%s\",\"priority\":0,\"step\":%d,\"type\":2,\"updated\":%s,\"userId\":\"%s\",\"DataSource\":2,\"exerciseTime\":0}]}",
                        step / 4, dateTimeFormat.format(date), dateFormat.format(date), step / 3, BotUtils.randomStr(32), dateTimeFormat.format(date), step, tenTime + "000", motionEntity.getLeXinUserId())),
                OkHttpUtils.addCookie(motionEntity.getLeXinCookie()));
        if (jsonObject.getInteger("code").equals(200)) return "步数修改成功！！";
        else return jsonObject.getString("msg");
    }

    @Override
    public String bindBand(MotionEntity motionEntity) throws IOException {
        List<String> list = new ArrayList<>();
        list.add("http://we.qq.com/d/AQC7PnaOelOaCg9Ux8c9Ew95yumTVfMcFuGCHMY-");
        list.add("http://we.qq.com/d/AQC7PnaOi9BLVrfJIiVTU8ENIbv_9Lmlqia1ToGc");
        list.add("http://we.qq.com/d/AQC7PnaOXQhy3VvzFeP5bZMKmAQrGE6NJWdK3Xnk");
        list.add("http://we.qq.com/d/AQC7PnaOaEXBdhkdXQvTRE1CO1fIqBuitbSSGt2r");
        list.add("http://we.qq.com/d/AQC7PnaOdI9h0tfCr0KRlb78ISAE9qcaZ3btHrJE");
        list.add("http://we.qq.com/d/AQC7PnaOsThRYksmQcvpa0klKFrupqaqKyEPm8nj");
        list.add("http://we.qq.com/d/AQC7PnaOk8V-FV7R4ix61GToC5fh5I151hvlsNf6");
        String qrcode = list.get((int) (Math.random() * list.size()));
        JSONObject jsonObject = OkHttpUtils.postJson("https://sports.lifesense.com/device_service/device_user/bind",
                OkHttpUtils.addJson(String.format("{\"qrcode\": \"%s\",\"userId\":\"%s\"}", qrcode, motionEntity.getLeXinUserId())),
                OkHttpUtils.addCookie(motionEntity.getLeXinCookie()));
        if (jsonObject.getInteger("code").equals(200)) return "绑定成功！！";
        else return jsonObject.getString("msg");
    }
}
