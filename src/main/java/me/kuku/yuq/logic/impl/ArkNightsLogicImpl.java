package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ArkNightsEntity;
import me.kuku.yuq.logic.ArkNightsLogic;
import me.kuku.yuq.logic.DdOcrCodeLogic;
import me.kuku.yuq.pojo.DdOcrPojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Headers;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArkNightsLogicImpl implements ArkNightsLogic {

	private final String url = "https://ak.hypergryph.com";

	@Inject
	private DdOcrCodeLogic ddOcrCodeLogic;

	@Override
	public Result<ArkNightsEntity> login(String account, String password) throws IOException {
		String referer = "https://ak.hypergryph.com/user/login";
		Response response = OkHttpUtils.get(referer);
		response.close();
		String allCookie = OkHttpUtils.getCookie(response);
		String csrfToken = OkHttpUtils.getCookie(allCookie, "csrf_token");
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("x-csrf-token", csrfToken);
		headerMap.put("referer", referer);
		headerMap.put("user-agent", UA.PC.getValue());
		headerMap.put("cookie", allCookie);
		Headers headers = OkHttpUtils.addHeaders(headerMap);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("account", account);
		jsonObject.put("password", password);
		Response beforeLoginResponse = OkHttpUtils.post("https://ak.hypergryph.com/user/api/user/login",
				OkHttpUtils.addJson(jsonObject.toString()), headers);
		JSONObject beforeLoginJsonObject = OkHttpUtils.getJson(beforeLoginResponse);
		Integer code = beforeLoginJsonObject.getInteger("code");
		if (code == 1100){
			// 验证码
			JSONObject dataJsonObject = beforeLoginJsonObject.getJSONObject("data");
			String gt = dataJsonObject.getString("gt");
			String challenge = dataJsonObject.getString("challenge");
			Result<DdOcrPojo> identifyResult = ddOcrCodeLogic.identify(gt, challenge, "https://ak.hypergryph.com/user/api/user/login");
			if (identifyResult.isFailure()) return Result.failure(identifyResult.getMessage());
			DdOcrPojo pojo = identifyResult.getData();
			jsonObject.put("geetest_challenge", pojo.getChallenge());
			jsonObject.put("geetest_seccode", pojo.getSecCode());
			jsonObject.put("geetest_validate", pojo.getValidate());
			Response loginResponse = OkHttpUtils.post("https://ak.hypergryph.com/user/api/user/login",
					OkHttpUtils.addJson(jsonObject.toString()), headers);
			JSONObject loginJsonObject = OkHttpUtils.getJson(loginResponse);
			if (loginJsonObject.getInteger("code") == 0){
				String cookie = OkHttpUtils.getCookie(loginResponse);
				ArkNightsEntity arkNightsEntity = new ArkNightsEntity(cookie);
				return Result.success(arkNightsEntity);
			}else return Result.failure(loginJsonObject.getString("msg"));
		}else if (code == 0){
			String cookie = OkHttpUtils.getCookie(beforeLoginResponse);
			ArkNightsEntity arkNightsEntity = new ArkNightsEntity(cookie);
			return Result.success(arkNightsEntity);
		}else return Result.failure(beforeLoginJsonObject.getString("msg"));
	}

	@Override
	public Result<String> akCookie(ArkNightsEntity arkNightsEntity, String source, String sourceUid) throws IOException {
		JSONObject jsonObject = OkHttpUtils.postJson("https://ak.hypergryph.com/user/api/sdk/user/getToken",
				new HashMap<>(), OkHttpUtils.addHeaders(arkNightsEntity.getCookie(), "https://ak.hypergryph.com/", UA.PC));
		if (jsonObject.getInteger("code") == 0){
			String token = jsonObject.getJSONObject("data").getString("token");
			JSONObject param = new JSONObject();
			param.put("hgToken", token);
			param.put("source", source);
			param.put("sourceUid", "58005820");
			Response response = OkHttpUtils.post("https://ak.hypergryph.com/activity/preparation/login/hg",
					OkHttpUtils.addJson(param.toString()), OkHttpUtils.addHeaders(arkNightsEntity.getCookie(), "https://ak.hypergryph.com/", UA.PC));
			JSONObject resultJsonObject = OkHttpUtils.getJson(response);
			if (resultJsonObject.getInteger("code") == 0){
				String cookie = OkHttpUtils.getCookie(response);
				return Result.success(cookie);
			}else return Result.failure(resultJsonObject.getString("msg"));
		}else return Result.failure(jsonObject.getString("msg"));
	}

	@Override
	public Result<List<Map<String, String>>> rechargeRecord(String cookie) throws IOException {
		Response response = OkHttpUtils.get(url + "/user/inquiryOrder", OkHttpUtils.addCookie(cookie));
		if (response.code() == 302) return Result.failure("cookie已失效！！", null);
		String html = OkHttpUtils.getStr(response);
		String jsonStr = BotUtils.regex("window.__INITIAL_DATA__ =", "</script>", html);
		JSONObject jsonObject = JSON.parseObject(jsonStr);
		JSONArray jsonArray = jsonObject.getJSONObject("inquiryOrder").getJSONArray("data");
		List<Map<String, String>> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			Map<String, String> map = new HashMap<>();
			map.put("amount", singleJsonObject.getString("amount"));
			map.put("productName", singleJsonObject.getString("productName"));
			map.put("payTime", singleJsonObject.getString("payTime") + "000");
			list.add(map);
		}
		return Result.success(list);
	}

	@Override
	public Result<List<Map<String, String>>> searchRecord(String cookie, Integer page) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson(url + "/user/api/inquiry/gacha?page=" + page, OkHttpUtils.addCookie(cookie));
		Integer code = jsonObject.getInteger("code");
		if (code == 0){
			List<Map<String, String>> list = new ArrayList<>();
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject singleJsonObject = jsonArray.getJSONObject(i);
				Map<String, String> map = new HashMap<>();
				map.put("ts", singleJsonObject.getString("ts") + "000");
				JSONArray charsJsonArray = singleJsonObject.getJSONArray("chars");
				StringBuilder result = new StringBuilder();
				for (int j = 0; j < charsJsonArray.size(); j++){
					JSONObject charJsonObject = charsJsonArray.getJSONObject(j);
					result.append(charJsonObject.getString("name")).append("--").append(charJsonObject.getInteger("rarity") + 1).append("星").append("/");
				}
				map.put("result", result.toString());
				list.add(map);
			}
			return Result.success(list);
		}else return Result.failure(jsonObject.getString("msg"), null);
	}

	@Override
	public Result<List<Map<String, String>>> sourceRecord(String cookie, Integer page) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson(url + "/user/api/inquiry/diamond?page=" + page, OkHttpUtils.addCookie(cookie));
		Integer code = jsonObject.getInteger("code");
		if (code == 0){
			List<Map<String, String>> list = new ArrayList<>();
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject singleJsonObject = jsonArray.getJSONObject(i);
				Map<String, String> map = new HashMap<>();
				map.put("ts", singleJsonObject.getString("ts") + "000");
				map.put("operation", singleJsonObject.getString("operation"));
				JSONObject changeJsonObject = singleJsonObject.getJSONArray("changes").getJSONObject(0);
				String coin = changeJsonObject.getString("before") + "->" + changeJsonObject.getString("after");
				map.put("coin", coin);
				list.add(map);
			}
			return Result.success(list);
		}else return Result.failure(jsonObject.getString("msg"), null);
	}
}
