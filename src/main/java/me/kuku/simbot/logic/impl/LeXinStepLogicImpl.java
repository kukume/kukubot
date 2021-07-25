package me.kuku.simbot.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.simbot.entity.StepEntity;
import me.kuku.simbot.logic.StepLogic;
import me.kuku.utils.DateTimeFormatterUtils;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service("leXin")
public class LeXinStepLogicImpl implements StepLogic {
	@Override
	public Result<StepEntity> login(String phone, String password) throws IOException {
		Response response = OkHttpUtils.post("https://sports.lifesense.com/sessions_service/login?screenHeight=2267&screenWidth=1080&systemType=2&version=4.5",
				OkHttpUtils.addJson(String.format("{\"password\":\"%s\",\"clientId\":\"%s\",\"appType\":6,\"loginName\":\"%s\",\"roleType\":0}", password, MyUtils.randomNum(32), phone)));
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		if (jsonObject.getInteger("code").equals(200)){
			return Result.success(StepEntity.Companion.getInstance(phone, password, OkHttpUtils.getCookie(response), jsonObject.getJSONObject("data").getString("userId"),
					jsonObject.getJSONObject("data").getString("accessToken")
			));
		}else return Result.failure(jsonObject.getString("msg"), null);
	}

	@Override
	public Result<String> modifyStepCount(StepEntity stepEntity, int step) throws IOException {
		String dateTimePattern = "yyyy-MM-dd hh:mm:ss";
		String datePattern = "yyyy-MM-dd";
		long time = System.currentTimeMillis();
		long tenTime = System.currentTimeMillis() / 1000;
		JSONObject jsonObject = OkHttpUtils.postJson("https://sports.lifesense.com/sport_service/sport/sport/uploadMobileStepV2?country=%E4%B8%AD%E5%9B%BD&city=%E6%9F%B3%E5%B7%9E&cityCode=450200&timezone=Asia%2FShanghai&latitude=24.368694&os_country=CN&channel=qq&language=zh&openudid=&platform=android&province=%E5%B9%BF%E8%A5%BF%E5%A3%AE%E6%97%8F%E8%87%AA%E6%B2%BB%E5%8C%BA&appType=6&requestId=" + MyUtils.randomStr(32) + "&countryCode=&systemType=2&longitude=109.532216&devicemodel=V1914A&area=CN&screenwidth=1080&os_langs=zh&provinceCode=450000&promotion_channel=qq&rnd=3d51742c&version=4.6.7&areaCode=450203&requestToken=" + MyUtils.randomStr(32) + "&network_type=wifi&osversion=10&screenheight=2267&ts=" + tenTime,
				OkHttpUtils.addJson(String.format("{\"list\":[{\"active\":1,\"calories\":%d,\"created\":\"%s\",\"dataSource\":2,\"dayMeasurementTime\":\"%s\",\"deviceId\":\"M_NULL\",\"distance\":%d,\"id\":\"%s\",\"isUpload\":0,\"measurementTime\":\"%s\",\"priority\":0,\"step\":%d,\"type\":2,\"updated\":%s,\"userId\":\"%s\",\"DataSource\":2,\"exerciseTime\":0}]}",
						step / 4, DateTimeFormatterUtils.format(time, dateTimePattern), DateTimeFormatterUtils.format(time, datePattern), step / 3, MyUtils.randomStr(32), DateTimeFormatterUtils.format(time, dateTimePattern), step, time, stepEntity.getLeXinUserid())),
				OkHttpUtils.addCookie(stepEntity.getLeXinCookie()));
		if (jsonObject.getInteger("code").equals(200)) return Result.success("步数修改成功！！", null);
		else return Result.failure(jsonObject.getString("msg"));
	}
}
