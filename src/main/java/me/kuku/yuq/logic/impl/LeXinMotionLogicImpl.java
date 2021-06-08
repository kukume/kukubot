package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.MotionEntity;
import me.kuku.yuq.logic.LeXinMotionLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class LeXinMotionLogicImpl implements LeXinMotionLogic {
    @Override
    public InputStream getCaptchaImage(String phone) throws IOException {
        return OkHttpUtils.getByteStream("https://sports.lifesense.com/sms_service/verify/getValidateCode?requestId=1000&sessionId=nosession&mobile=" + phone);
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
        String tenTime = String.valueOf(System.currentTimeMillis()).substring(0, 10);
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
