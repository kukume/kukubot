package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.logic.IdentifyCodeLogic;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.utils.OkHttpUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Base64;

public class TtIdentifyCodeLogicImpl implements IdentifyCodeLogic {

	@Inject
	private ConfigService configService;

	@Override
	public Result<String> identify(String type, byte[] bytes) throws IOException {
		ConfigEntity configEntity = configService.findByType(ConfigType.IdentifyCode.getType());
		if (configEntity == null) return Result.failure("没有绑定图鉴的信息，无法获取！！");
		JSONObject contentJsonObject = configEntity.getContentJsonObject();
		String username = contentJsonObject.getString("username");
		String password = contentJsonObject.getString("password");
		JSONObject params = new JSONObject();
		params.put("username", username);
		params.put("password", password);
		params.put("typeid", type);
		params.put("image", Base64.getEncoder().encodeToString(bytes));
		JSONObject jsonObject = OkHttpUtils.postJson("http://api.ttshitu.com/base64",
				OkHttpUtils.addJson(params.toString()));
		if (jsonObject.getInteger("code") == 0){
			return Result.success(jsonObject.getJSONObject("data").getString("result"));
		}else return Result.failure(jsonObject.getString("message"));
	}
}
