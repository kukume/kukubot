package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.logic.DCloudLogic;
import me.kuku.yuq.pojo.DCloudPojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class DCloudLogicImpl implements DCloudLogic {

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
		if (jsonObject.getInteger("ret") == 0){
			String cookie = OkHttpUtils.getCookie(response);
			String token = jsonObject.getString("token");
			dCloudPojo.setCookie(cookie);
			dCloudPojo.setToken(token);
			return Result.success(dCloudPojo);
		}else return Result.failure(jsonObject.getString("desc"));
	}

	@Override
	public Result<String> upload(DCloudPojo dCloudPojo, String spaceId, String name, byte[] bytes) {
		JSONObject jsonObject;
		Map<String, String> map = new HashMap<>();
		map.put("token", dCloudPojo.getToken());
		map.put("cookie", dCloudPojo.getCookie());
		map.put("user-agent", UA.PC.getValue());
		map.put("referer", "https://unicloud.dcloud.net.cn/cloud-storage?platform=aliyun&appid=");
		try {
			jsonObject = OkHttpUtils.getJson("https://unicloud.dcloud.net.cn/unicloud/api/file/upload-info?spaceId=" +
					spaceId + "&appid=&provider=aliyun&name=" + URLEncoder.encode(name, "utf-8") +"&size=" +
					bytes.length, map);
		} catch (Exception e) {
			return Result.failure("cookie失效，上传失败！！");
		}
		if (jsonObject.getInteger("ret") == 0){
			JSONObject dataJsonObject = jsonObject.getJSONObject("data");
			String signUrl = dataJsonObject.getString("SignUrl");
			String fileId = dataJsonObject.getString("Id");
			Response response = null;
			try {
				response = OkHttpUtils.put(signUrl, RequestBody.create(bytes, MediaType.parse("application/octet-stream")),
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
}
