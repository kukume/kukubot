package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.logic.DdOcrCodeLogic;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.DdOcrPojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.utils.OkHttpUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DdOcrCodeLogicImpl implements DdOcrCodeLogic {

	@Inject
	private ConfigService configService;

	@Override
	public Result<DdOcrPojo> identify(String gt, String challenge, String referer) throws IOException {
		ConfigEntity configEntity = configService.findByType(ConfigType.DdOcrCode.getType());
		if (configEntity == null) return Result.failure("机器人没有配置滴滴打码的信息，无法打码！！");
		Map<String, String> map = new HashMap<>();
		map.put("wtype", "geetest");
		map.put("secretkey", configEntity.getContent());
		map.put("gt", gt);
		map.put("challenge", challenge);
		map.put("referer", referer);
		JSONObject jsonObject = OkHttpUtils.postJson("http://api.faka168.com/api/gateway.jsonp",
				map);
		if (jsonObject.getInteger("status") == 0){
			JSONObject data = jsonObject.getJSONObject("data");
			return Result.success(new DdOcrPojo(data.getString("challenge"),
					data.getString("validate"), data.getString("type")));
		}else return Result.failure(jsonObject.getString("msg"));
	}
}
