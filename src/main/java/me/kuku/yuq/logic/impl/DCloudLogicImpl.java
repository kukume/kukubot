package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.logic.DCloudLogic;
import me.kuku.yuq.logic.CodeLogic;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.DCloudPojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class DCloudLogicImpl implements DCloudLogic {

	@Inject
	private CodeLogic codeLogic;
	@Inject
	private ConfigService configService;

	@Override
	public DCloudPojo getData() throws IOException {
		Response response = OkHttpUtils.get("https://unicloud.dcloud.net.cn/unicloud/api/captcha/uc?t=" +
				System.currentTimeMillis(), OkHttpUtils.addUA(UA.PC));
		byte[] bytes = OkHttpUtils.getBytes(response);
		String cookie = OkHttpUtils.getCookie(response);
		return new DCloudPojo(bytes, cookie);
	}

	@Override
	public Result<DCloudPojo> login(DCloudPojo dCloudPojo, String email, String password, String code) throws IOException {
		Response response = OkHttpUtils.post("https://unicloud.dcloud.net.cn/unicloud/api/user/login?email=" +
						email + "&password=" + password + "&verifycode=" + code, new HashMap<>(),
				OkHttpUtils.addHeaders(dCloudPojo.getCookie(), "https://unicloud.dcloud.net.cn/login",
						UA.PC));
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		Integer ret = jsonObject.getInteger("ret");
		if (ret == 0){
			String cookie = OkHttpUtils.getCookie(response);
			String token = jsonObject.getString("token");
			dCloudPojo.setCookie(cookie);
			dCloudPojo.setToken(token);
			return Result.success(dCloudPojo);
		} else if (ret == 1002){
			return Result.failure(501, "验证码错误，请重试！！");
		}else return Result.failure(jsonObject.getString("desc"));
	}

	@Override
	public Result<String> upload(DCloudPojo dCloudPojo, String spaceId, String name, InputStream is, Integer size) {
		JSONObject jsonObject;
		Map<String, String> map = new HashMap<>();
		map.put("token", dCloudPojo.getToken());
		map.put("cookie", dCloudPojo.getCookie());
		map.put("user-agent", UA.PC.getValue());
		map.put("referer", "https://unicloud.dcloud.net.cn/cloud-storage?platform=aliyun&appid=");
		try {
			if (size == null) size = is.available();
			jsonObject = OkHttpUtils.getJson("https://unicloud.dcloud.net.cn/unicloud/api/file/upload-info?spaceId=" +
					spaceId + "&appid=&provider=aliyun&name=" + URLEncoder.encode(name, "utf-8") +"&size=" +
					size, map);
		} catch (Exception e) {
			return Result.failure(502, "cookie失效，上传失败！！");
		}
		if (jsonObject.getInteger("ret") == 0){
			JSONObject dataJsonObject = jsonObject.getJSONObject("data");
			String signUrl = dataJsonObject.getString("SignUrl");
			String fileId = dataJsonObject.getString("Id");
			Response response;
			try {
				response = OkHttpUtils.put(signUrl, OkHttpUtils.getStreamBody(is),
						OkHttpUtils.addHeaders(null, "https://unicloud.dcloud.net.cn/", UA.PC));
				response.close();
				if (response.code() == 200){
					JSONObject resultJsonObject = OkHttpUtils.postJson("https://unicloud.dcloud.net.cn/unicloud/api/file/register?provider=aliyun&spaceId=" +
							spaceId + "&appid=&fileId=" + fileId, new HashMap<>(), OkHttpUtils.addHeaders(map));
					if (resultJsonObject.getInteger("ret") == 0){
						String url = signUrl.substring(0, signUrl.indexOf("?"));
						url = url.replace(BotUtils.regex("://", "/", url), "vkceyugu.cdn.bspapp.com");
						return Result.success(url);
					}else return Result.failure("上传失败，请稍后重试！！");
				}else {
					return Result.failure("上传失败，请稍后重试！！");
				}
			} catch (IOException e) {
				return Result.failure("上传失败，请稍后重试！！");
			}
		}else return Result.failure(jsonObject.getString("desc"));
	}

	@Override
	public Result<DCloudPojo> reLogin() throws IOException {
		return reLogin(0);
	}

	public Result<DCloudPojo> reLogin(int num) throws IOException {
		if (num > 2) return Result.failure("验证码验证失败，请重试！！");
		DCloudPojo dCloudPojo = getData();
		Result<String> identifyResult = codeLogic.identify("1003", dCloudPojo.getCaptchaImage());
		if (identifyResult.isFailure()) return Result.failure(identifyResult.getMessage());
		ConfigEntity configEntity = configService.findByType(ConfigType.DCloud.getType());
		if (configEntity == null) return Result.failure("没有找到您的dcloud账号，无法重新登录！！");
		JSONObject contentJsonObject = configEntity.getContentJsonObject();
		Result<DCloudPojo> loginResult = login(dCloudPojo, contentJsonObject.getString("email"), contentJsonObject.getString("password"), identifyResult.getData());
		if (loginResult.getCode() == 501){
			return reLogin(++num);
		}else if (loginResult.isSuccess()){
			DCloudPojo pojo = loginResult.getData();
			contentJsonObject.put("cookie", pojo.getCookie());
			contentJsonObject.put("token", pojo.getToken());
			configEntity.setContentJsonObject(contentJsonObject);
			configService.save(configEntity);
			return loginResult;
		}
		else return loginResult;
	}
}
