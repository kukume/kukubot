package me.kuku.simbot.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.UA;
import me.kuku.simbot.entity.KeyTapEntity;
import me.kuku.simbot.logic.KeyTapLogic;
import me.kuku.simbot.pojo.KeyTapQrcode;
import me.kuku.utils.*;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class KeyTapLogicImpl implements KeyTapLogic {

	private final String SIGN_KEY = "&key=FdjydGAAKasmht1nFnR4MS5itFeh4R1Lk";
	private final String RSA_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCf5viGpYn1duRt9wzwca1SEuL+wwnBfBfza0nTuLPYR5uZyheUoFI+cudN9eB4jlvXij4yAxH59ML8BhVUab/j+TmeDsCe+OLpswdHWEXtY1HacLpw/wpsKQHBQZYhAARZRx/4J5/fiz/pJcH5qVGYK0Yu8c9CNl9/eHDQkj9LoQIDAQAB";

	private Response request(String url, JSONObject params) throws IOException {
		String aesKey = MyUtils.random(16);
		params.put("appKey", "CuGsbe6HdAe6vDBHFew2Di");
		long time = System.currentTimeMillis();
		params.put("nonce", String.valueOf(time));
		params.put("timestamp", time);
		List<String> signList = new ArrayList<>();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String || value instanceof Long){
				signList.add(entry.getKey() + "=" + value);
			}
		}
		Collections.sort(signList);
		String sign = StringUtils.join(signList, '&');
		sign += SIGN_KEY;
		params.put("sign", MD5Utils.toMD5(sign));
		Map<String, String> headers = new HashMap<>();
		try {
			headers.put("X-Key", RSAUtils.encrypt(aesKey, RSAUtils.getPublicKey(RSA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
		}
//		headers.put("X-Session-Ticket", RSAUtils.encrypt(aesKey, RSAUtils.getPublicKey(RSA_KEY)));
		return OkHttpUtils.post(url,
				OkHttpUtils.addEncryptedJson(AESUtils.aesEncryptBase(aesKey, params.toJSONString())),
				OkHttpUtils.addHeaders(headers));
	}

	private JSONObject requestJson(String url, JSONObject params) throws IOException {
		Response response = request(url, params);
		return OkHttpUtils.getJson(response);
	}

	@Override
	public KeyTapQrcode getQrcode() throws IOException {
		String deviceId = MyUtils.randomStr(32);
		JSONObject params = new JSONObject();
		params.put("appId", "usercenter-web");
		params.put("context", new JSONObject(){{
			put("deviceId", deviceId);
		}});
		JSONObject jsonObject = requestJson("https://id.heytap.com/api/scan-login/v1/general-qrcode", params);
		JSONObject dataJsonObject = jsonObject.getJSONObject("data");
		String qid = dataJsonObject.getString("qid");
		String url = dataJsonObject.getString("qrcodeUrl");
		return new KeyTapQrcode(url, deviceId, qid);
	}

	@Override
	public Result<?> checkQrcode(KeyTapQrcode keyTapQrcode) throws IOException {
		//  getJSONObject("error").getInteger("code")
		// 2310501  未扫描
		// 2310503 已失效
		// 2310502 已扫描
		//
		JSONObject params = new JSONObject();
		params.put("qid", keyTapQrcode.getQid());
		params.put("deviceId", keyTapQrcode.getDeviceId());
		params.put("callbackUrl", "https://www.heytap.com/cn/web/");
		Response response = request("https://id.heytap.com/api/scan-login/v1/check-qrcode", params);
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		if (jsonObject.getBoolean("success")){
			StringBuilder cookieSb = new StringBuilder(OkHttpUtils.getCookie(response));
			String key = keyTapQrcode.getQid();
			Map<String, String> ppp = new HashMap<>();
			ppp.put("key", key);
			Response resp = OkHttpUtils.post("https://e.heytap.com/loginCallback", ppp,
					OkHttpUtils.addCookie(cookieSb.toString()));
			System.out.println(OkHttpUtils.getStr(resp));
			System.out.println(OkHttpUtils.getCookie(resp));
			List<String> urls = new ArrayList<>();
			urls.add("https://htsg-storeapi-sg.heytap.com/mall/account/set/cookie");
			urls.add("https://api.heythings-iot.com/platform/manufacturer/login/callback/setcookie");
			urls.add("https://api.heythings-iot.com/platform/manufacturer/login/callback/setcookie");
			// https://open.oppomobile.com/user/cookiecallback/setcookiewithjson
			urls.add("https://id.realme.com/api/cookie/sync-cookie");
			urls.add("https://a.cpc.heytapmobi.com/node/account/callback/postHeytapLogin");
			Map<String, String> headers = new HashMap<>();
			headers.put("referer", "https://id.heytap.com/");
			headers.put("cookie", cookieSb.toString());
			headers.put("user-agent", UA.PC.getValue());
			headers.put("Content-Length", "35");
			for (String url : urls) {
				Response cookieResponse = OkHttpUtils.post(url, ppp, OkHttpUtils.addHeaders(headers));
				cookieResponse.close();
				String tempCookie = OkHttpUtils.getCookie(cookieResponse);
				cookieSb.append(tempCookie);
			}
			String cookie = cookieSb.toString();
			return Result.success(new KeyTapEntity(cookie, keyTapQrcode.getDeviceId()));
		}else {
			Integer code = jsonObject.getJSONObject("error").getInteger("code");
			if (code == 2310501 || code == 2310502) return Result.failure(0, "已扫描或未扫描");
			else if (code == 2310503) return Result.failure("已失效");
			else return Result.failure("未知的代码");
		}
	}
}
