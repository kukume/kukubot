package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.utils.OkHttpUtils;
import me.kuku.yuq.entity.OfficeGlobalEntity;
import me.kuku.yuq.entity.OfficeRole;
import me.kuku.yuq.exception.VerifyFailedException;
import me.kuku.yuq.logic.OfficeGlobalLogic;
import me.kuku.yuq.pojo.OfficeToken;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OfficeGlobalLogicImpl implements OfficeGlobalLogic {
	private final Map<String, OfficeToken> cache = new HashMap<>();

	private String getToken(OfficeGlobalEntity officePojo) throws IOException {
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
	public Result<?> createUser(OfficeGlobalEntity officePojo, String displayName, String username, String password, Integer index) throws IOException {
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
					OkHttpUtils.addJson("{\"addLicenses\":[{\"disabledPlans\":[],\"skuId\":\"" + officePojo.getSKuJson().get(index).getId() + "\"}],\"removeLicenses\":[]}"),
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

	private Result<String> getUserid(OfficeGlobalEntity officePojo, String mail) throws IOException {
		String token = getToken(officePojo);
		JSONObject jsonObject = OkHttpUtils.getJson("https://graph.microsoft.com/v1.0/users/" + mail,
				OkHttpUtils.addSingleHeader("Authorization", token));
		if (jsonObject.containsKey("error")){
			return Result.failure(jsonObject.getJSONObject("error").getString("message"));
		}else return Result.success(jsonObject.getString("id"));
	}

	@Override
	public Result<?> userToAdmin(OfficeGlobalEntity officePojo, String mail, OfficeRole officeRole) throws IOException {
		//roletemplateID
		//useradmin:fe930be7-5e62-47db-91af-98c3a49a38b1
		//globalAdmin:62e90394-69f5-4237-9190-012177145e10
		//Privileged role Admin:e8611ab8-c189-46e8-94e1-60213ab1f814
		String token = getToken(officePojo);
		Result<String> idResult = getUserid(officePojo, mail);
		if (idResult.isFailure()) return idResult;
		Response response = OkHttpUtils.post("https://graph.microsoft.com/v1.0/directoryRoles/roleTemplateId=" + officeRole.getValue() + "/members/$ref",
				OkHttpUtils.addJson(new JSONObject() {{
					put("@odata.id", "https://graph.microsoft.com/v1.0/directoryObjects/" + idResult.getData());
				}}.toJSONString()), OkHttpUtils.addSingleHeader("Authorization", token));
		if (response.code() == 204) {
			response.close();
			return Result.success("升级该用户为管理员成功！", null);
		} else {
			JSONObject jsonObject = OkHttpUtils.getJson(response);
			return Result.failure(jsonObject.getJSONObject("error").getString("message"));
		}
	}
}
