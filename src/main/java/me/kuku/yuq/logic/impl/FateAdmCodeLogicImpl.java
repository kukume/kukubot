package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.logic.CodeLogic;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.utils.MD5Utils;
import me.kuku.yuq.utils.OkHttpUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Named("fateAdm")
public class FateAdmCodeLogicImpl implements CodeLogic {
	@Inject
	private ConfigService configService;

	@Override
	public Result<String> identify(String type, byte[] bytes) throws IOException {
		ConfigEntity configEntity = configService.findByType(ConfigType.FateAdmCode.getType());
		if (configEntity == null) return Result.failure("机器人没有配置fateAdmin打码的信息，无法打码！！");
		JSONObject contentJsonObject = configEntity.getContentJsonObject();
		String pdId = contentJsonObject.getString("pdId");
		String pdKey = contentJsonObject.getString("pdKey");
		Map<String, String> map = new HashMap<>();
		long l = System.currentTimeMillis() / 1000;
		map.put("user_id", pdId);
		map.put("timestamp", String.valueOf(l));
		map.put("sign", sign(pdId, pdKey, l));
		map.put("predict_type", type);
		map.put("img_data", Base64.getEncoder().encodeToString(bytes));
		JSONObject jsonObject = OkHttpUtils.postJson("http://pred.fateadm.com/api/capreg",
				map);
		if (jsonObject.getInteger("RetCode") == 0)
			return Result.success(jsonObject.getJSONObject("RspData").getString("result"));
		else return Result.failure(jsonObject.getString("ErrMsg"));
	}



	private String sign(String pdId, String pdKey, long l){
		return MD5Utils.toMD5(pdId + l + MD5Utils.toMD5(l + pdKey));
	}
}
