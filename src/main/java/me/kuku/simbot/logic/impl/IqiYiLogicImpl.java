package me.kuku.simbot.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.simbot.entity.IqiYiEntity;
import me.kuku.simbot.logic.IqiYiLogic;
import me.kuku.simbot.pojo.IqiYiQrcode;
import me.kuku.utils.MD5Utils;
import me.kuku.utils.OkHttpUtils;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service
public class IqiYiLogicImpl implements IqiYiLogic {
	@Override
	public IqiYiQrcode getQrcode() throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("agenttype", "1");
		params.put("app_version", "");
		params.put("device_name", "网页端");
		params.put("fromSDK", "1");
		params.put("ptid", "01010021010000000000");
		params.put("sdk_version", "1.0.0");
		params.put("surl", "1");
		JSONObject jsonObject = OkHttpUtils.postJson("https://passport.iqiyi.com/apis/qrcode/gen_login_token.action", params,
				OkHttpUtils.addReferer("https://www.iqiyi.com/"));
		JSONObject dataJsonObject = jsonObject.getJSONObject("data");
		String token = dataJsonObject.getString("token");
		String url = dataJsonObject.getString("url");
		String enUrl = URLEncoder.encode(url, "utf-8");
		String salt = MD5Utils.toMD5("35f4223bb8f6c8638dc91d94e9b16f5" + enUrl);
		url = "https://qrcode.iqiyipic.com/login/?data=" + enUrl + "&property=0&salt=" + salt + "&width=162&_=0.15377144503723406";
		return new IqiYiQrcode(token, url, dataJsonObject.getLong("expire"));
	}

	@Override
	public Result<IqiYiEntity> checkQrcode(IqiYiQrcode iqiYiQrcode) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("agenttype", "1");
		params.put("app_version", "");
		params.put("fromSDK", "1");
		params.put("ptid", "01010021010000000000");
		params.put("sdk_version", "1.0.0");
		params.put("token", iqiYiQrcode.getToken());
		Response response = OkHttpUtils.post("https://passport.iqiyi.com/apis/qrcode/is_token_login.action",
				params, OkHttpUtils.addReferer("https://www.iqiyi.com/"));
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		System.out.println(jsonObject);
		switch (jsonObject.getString("code")){
			case "A00000":
				String cookie = OkHttpUtils.getCookie(response);
				String pOne = OkHttpUtils.getCookie(cookie, "P00001");
				String pThree = OkHttpUtils.getCookie(cookie, "P00003");
				IqiYiEntity iqiYiEntity = new IqiYiEntity(cookie, pOne, pThree);
				return Result.success(iqiYiEntity);
			case "A00001":
			case "P01006":
				return Result.failure(0, "未扫描或已被扫描！");
			case "P01007":
				return Result.failure("二维码已被拒绝");
			case "P00501":
				return Result.failure("二维码已过期");
			case "P01005":
				return Result.failure("二维码已被使用过");
		}
		return Result.failure("新出的代码二维码检测？");
	}
}
