package me.kuku.simbot.logic.impl;

import com.alibaba.fastjson.JSONArray;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
		switch (jsonObject.getString("code")){
			case "A00000":
				String cookie = OkHttpUtils.getCookie(response);
				String pOne = OkHttpUtils.getCookie(cookie, "P00001");
				String pThree = OkHttpUtils.getCookie(cookie, "P00003");
				IqiYiEntity iqiYiEntity = IqiYiEntity.Companion.getInstance(cookie, pOne, pThree);
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

	@Override
	public Result<Void> sign(IqiYiEntity iqiYiEntity) throws IOException {
		Map<String, String> map = new HashMap<>();
		map.put("P00001", iqiYiEntity.getPOne());
		map.put("autoSign", "yes");
		JSONObject jsonObject = OkHttpUtils.postJson("https://tc.vip.iqiyi.com/taskCenter/task/queryUserTask", map);
		if (jsonObject.getString("code").equals("A00000"))
			return Result.success();
		else return Result.failure(jsonObject.getJSONObject("data").getJSONObject("signInfo").getString("msg"));
	}

	@Override
	public Result<Void> task(IqiYiEntity iqiYiEntity) throws IOException {
		Map<String, String> map = new HashMap<>();
		map.put("P00001", iqiYiEntity.getPOne());
		JSONObject jsonObject = OkHttpUtils.postJson("https://tc.vip.iqiyi.com/taskCenter/task/queryUserTask", map);
		if (!jsonObject.getString("code").equals("A00000"))
			return Result.failure(jsonObject.getJSONObject("data").getJSONObject("signInfo").getString("msg"));
		JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("tasks").getJSONArray("daily");
		List<Map<String, String>> tasks = new ArrayList<>();
		jsonArray.stream().map(it -> (JSONObject) it).forEach(it -> {
			Map<String, String> task = new HashMap<>();
			task.put("name", it.getString("name"));
			task.put("taskCode", it.getString("taskCode"));
			task.put("status", it.getString("status"));
			task.put("taskReward", it.getJSONObject("taskReward").getString("task_reward_growth"));
			tasks.add(task);
		});
		String url = "https://tc.vip.iqiyi.com/taskCenter/task/joinTask";
		String url1 = "https://tc.vip.iqiyi.com/taskCenter/task/notify";
		Map<String, String> params = new HashMap<>();
		Map<String, String> params1 = new HashMap<>();
		params.put("P00001", iqiYiEntity.getPOne());
		params.put("taskCode", "");
		params.put("platform", "bb136ff4276771f3");
		params.put("lang", "zh_CN");
		params1.put("P00001", iqiYiEntity.getPOne());
		params1.put("taskCode", "");
		params1.put("platform", "bb136ff4276771f3");
		params1.put("bizSource", "gphoneviptask");
		params1.put("lang", "cn");
		for (Map<String, String> task : tasks) {
			if ("2".equals(task.get("status")) && !"夸夸打赏".equals(task.get("name")) && !"观影30分钟".equals(task.get("name"))){
				params.put("taskCode", task.get("taskCode"));
				params1.put("taskCode", task.get("taskCode"));
				JSONObject json = OkHttpUtils.postJson(url, params);
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				OkHttpUtils.postJson(url1, params1);
			}else if ("4".equals(task.get("status")) && !"夸夸打赏".equals(task.get("name")) && !"观影30分钟".equals(task.get("name"))){
				params1.put("taskCode", task.get("taskCode"));
				JSONObject json = OkHttpUtils.postJson(url1, params1);
			}
		}
		String resultUrl = "https://tc.vip.iqiyi.com/taskCenter/task/getTaskRewards";
		Map<String, String> resultParams = new HashMap<>();
		resultParams.put("P00001", iqiYiEntity.getPOne());
		resultParams.put("taskCode", "");
		resultParams.put("platform", "bb136ff4276771f3");
		resultParams.put("lang", "zh_CN");
		for (Map<String, String> task : tasks) {
			if ("0".equals(task.get("status"))) {
				resultParams.put("taskCode", task.get("taskCode"));
				JSONObject json = OkHttpUtils.postJson(resultUrl, resultParams);
			}
		}
		return Result.success();
	}

	@Override
	public Result<Void> draw(IqiYiEntity iqiYiEntity) throws IOException {
		String url = "https://iface2.iqiyi.com/aggregate/3.0/lottery_activity?lottery_chance=1&app_k=b398b8ccbaeacca840073a7ee9b7e7e6&app_v=11.6.5&platform_id=10&dev_os=8.0.0&dev_ua=FRD-AL10&net_sts=1&qyid=2655b332a116d2247fac3dd66a5285011102&psp_uid=" +
				iqiYiEntity.getPThree() + "&psp_cki=" + iqiYiEntity.getPOne() + "&psp_status=3&secure_v=1&secure_p=GPhone&req_sn=" + System.currentTimeMillis();
		JSONObject jsonObject = OkHttpUtils.getJson(url);
		if (jsonObject.getInteger("code") == 0){
			Integer chance = jsonObject.getInteger("daysurpluschance");
			for (int i = 0; i < chance; i++){
				OkHttpUtils.getJson(url.replace("lottery_chance=1&", ""));
			}
			return Result.success();
		}else return Result.failure(jsonObject.getString("errorReason"));
	}
}
