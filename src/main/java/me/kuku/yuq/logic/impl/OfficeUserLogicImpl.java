package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.exception.VerifyFailedException;
import me.kuku.yuq.logic.OfficeUserLogic;
import me.kuku.yuq.pojo.OfficeToken;
import me.kuku.yuq.pojo.OfficePojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OfficeUserLogicImpl implements OfficeUserLogic {

	private final Map<String, OfficeToken> cache = new HashMap<>();

	private String getToken(OfficePojo officePojo) throws IOException {
		boolean isUpdate = false;
		String clientId = officePojo.getClientId();
		OfficeToken token = cache.get(clientId);
		if (token == null) isUpdate = true;
		else {
			if (System.currentTimeMillis() > token.getExpiresTime()) isUpdate = true;
		}
		if (isUpdate) {
			String url = "https://login.microsoftonline.com/" + officePojo.getTenantId() + "/oauth2/v2.0/token";
			Map<String, String> map = new HashMap<>();
			map.put("grant_type", "client_credentials");
			map.put("client_id", officePojo.getClientId());
			map.put("client_secret", officePojo.getClientSecret());
			map.put("scope", "https://graph.microsoft.com/.default");
			JSONObject jsonObject = OkHttpUtils.postJson(url, map);
			if (jsonObject.containsKey("access_token")) {
				long time = jsonObject.getLong("expires_in") * 1000;
				OfficeToken officeToken = new OfficeToken(jsonObject.getString("token_type") + " " +
						jsonObject.getString("access_token"),
						System.currentTimeMillis() + time);
				cache.put(clientId, officeToken);
				return officeToken.getAccessToken();
			} else throw new VerifyFailedException(jsonObject.getString("error_description"));
		}else return token.getAccessToken();
	}

	@Override
	public Result<?> createUser(OfficePojo officePojo, String displayName, String username, String password, Integer index) throws IOException {
		if (index == null) index = 0;
		Headers headers = OkHttpUtils.addSingleHeader("Authorization",
				getToken(officePojo));
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("accountEnabled", true);
		jsonObject.put("displayName", displayName);
		jsonObject.put("mailNickname", username);
		jsonObject.put("passwordPolicies", "DisablePasswordExpiration, DisableStrongPassword");
		JSONObject innerJsonObject = new JSONObject();
		innerJsonObject.put("password", password);
		innerJsonObject.put("forceChangePasswordNextSignIn", true);
		jsonObject.put("passwordProfile", innerJsonObject);
		String email = username + "@" + officePojo.getDomain();
		jsonObject.put("userPrincipalName", email);
		jsonObject.put("usageLocation", "CN");
		Response response = OkHttpUtils.post("https://graph.microsoft.com/v1.0/users",
				OkHttpUtils.addJson(jsonObject.toJSONString()), headers);
		if (response.code() == 201){
			response.close();
			Response finallyResponse = OkHttpUtils.post("https://graph.microsoft.com/v1.0/users/" + email + "/assignLicense",
					OkHttpUtils.addJson("{\"addLicenses\":[{\"disabledPlans\":[],\"skuId\":\"" + officePojo.getSku().get(index).getId() + "\"}],\"removeLicenses\":[]}"),
					headers);
			if (finallyResponse.code() == 200){
				finallyResponse.close();
				return Result.success("创建账号成功！！", null);
			}else {
				JSONObject resultJsonObject = OkHttpUtils.getJson(finallyResponse);
				return Result.failure("创建账号成功，分配许可证失败！" + resultJsonObject.getJSONObject("error").getString("message"));
			}
		}else {
			JSONObject resultJsonObject = OkHttpUtils.getJson(response);
			return Result.failure("创建账号失败！" + resultJsonObject.getJSONObject("error").getString("message"));
		}
	}
}
