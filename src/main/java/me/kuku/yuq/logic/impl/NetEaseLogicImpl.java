package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.utils.AESUtils;
import me.kuku.utils.IOUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.yuq.entity.NetEaseEntity;
import me.kuku.yuq.logic.NetEaseLogic;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.NetEaseQrcode;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NetEaseLogicImpl implements NetEaseLogic {

	@Inject
	private ToolLogic toolLogic;

	private final String api = "https://netease.kukuqaq.com";
	private final String referer = "https://music.163.com/";
	private final String UA = me.kuku.pojo.UA.PC.getValue();

	private String aesEncode(String secretData, String secret){
		String vi = "0102030405060708";
		return AESUtils.encrypt(secretData, secret, vi);
	}

	private Map<String, String> prepare(Map<String, String> map){
		String nonce = "0CoJUm6Qyw8W8jud";
		String secretKey = "TA3YiYCfY2dDJQgg";
		String encSecKey = "84ca47bca10bad09a6b04c5c927ef077d9b9f1e37098aa3eac6ea70eb59df0aa28b691b7e75e4f1f9831754919ea784c8f74fbfadf2898b0be17849fd656060162857830e241aba44991601f137624094c114ea8d17bce815b0cd4e5b8e2fbaba978c6d1d14dc3d1faf852bdd28818031ccdaaa13a6018e1024e2aae98844210";
		String param = aesEncode(JSON.toJSONString(map), nonce);
		param = aesEncode(param, secretKey);
		HashMap<String, String> resultMap = new HashMap<>();
		resultMap.put("params", param);
		resultMap.put("encSecKey", encSecKey);
		return resultMap;
	}

	private NetEaseEntity getEntityByResponse(Response response){
		String cookie = OkHttpUtils.getCookie(response);
		Map<String, String> cookieMap = OkHttpUtils.getCookie(cookie, "MUSIC_U", "__csrf");
		return NetEaseEntity.Companion.getInstance(
				cookieMap.get("MUSIC_U"), cookieMap.get("__csrf")
		);
	}

	@Override
	public Result<NetEaseEntity> loginByPhone(String phone, String password) throws IOException {
		Response response = OkHttpUtils.get(api + "/login/cellphone?phone=" + phone + "&md5_password=" + password);
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		if (jsonObject.getInteger("code") == 200){
			return Result.success(getEntityByResponse(response));
		}else return Result.failure("登录失败，请重试！", null);
	}

	@Override
	public Result<NetEaseEntity> loginByEmail(String email, String password) throws IOException {
		Response response = OkHttpUtils.get(api + "/login?email=" + email + "&md5_password=" + password);
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		if (jsonObject.getInteger("code") == 200){
			return Result.success(getEntityByResponse(response));
		}else return Result.failure(jsonObject.getString("msg"), null);
	}

	@Override
	public NetEaseQrcode loginByQrcode() throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson(api + "/login/qr/key?_=" + System.currentTimeMillis());
		String key = jsonObject.getJSONObject("data").getString("unikey");
		JSONObject resultJsonObject = OkHttpUtils.getJson(api + "/login/qr/create?key=" + key + "&_=" + System.currentTimeMillis());
		String url = resultJsonObject.getJSONObject("data").getString("qrurl");
		InputStream is = toolLogic.creatQr(url);
		byte[] bytes = IOUtils.read(is);
		return new NetEaseQrcode(bytes, key);
	}

	@Override
	public Result<NetEaseEntity> checkQrcode(NetEaseQrcode netEaseQrcode) throws IOException {
		Response response = OkHttpUtils.get(api + "/login/qr/check?key=" + netEaseQrcode.getKey() + "&_=" + System.currentTimeMillis());
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		Integer code = jsonObject.getInteger("code");
		switch (code){
			case 801:
			case 802:
				return Result.failure(0, "未扫码或正在验证中");
			case 800:
				return Result.failure("二维码已失效！");
			case 803:
				return Result.success(getEntityByResponse(response));
		}
		return Result.failure("未知的错误代码，错误代码为" + code + "，" + jsonObject.getString("message"));
	}

	@Override
	public Result<?> sign(NetEaseEntity netEaseEntity) throws IOException {
		OkHttpUtils.get(api + "/daily_signin?type=1",
				OkHttpUtils.addCookie(netEaseEntity.getCookie())).close();
		OkHttpUtils.get(api + "/yunbei/sign",
				OkHttpUtils.addCookie(netEaseEntity.getCookie())).close();
		JSONObject jsonObject = OkHttpUtils.getJson(api + "/daily_signin?type=0",
				OkHttpUtils.addCookie(netEaseEntity.getCookie()));
		Integer code = jsonObject.getInteger("code");
		switch (code){
			case 200: return Result.success("签到成功！！", null);
			case -2: return Result.success("今日已签到", null);
			default: return Result.failure(jsonObject.getString("msg"));
		}
	}

	private Result<List<String>> recommend(NetEaseEntity netEaseEntity) throws IOException {
		Map<String, String> map = new HashMap<>();
		map.put("csrf_token", netEaseEntity.getCsrf());
		JSONObject jsonObject = OkHttpUtils.postJson("https://music.163.com/weapi/v1/discovery/recommend/resource",
				prepare(map), OkHttpUtils.addHeaders(netEaseEntity.getCookie(), referer, UA));
		int code = jsonObject.getInteger("code");
		if (code == 200){
			JSONArray jsonArray = jsonObject.getJSONArray("recommend");
			List<String> list = new ArrayList<>();
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject singleJsonObject = jsonArray.getJSONObject(i);
				list.add(singleJsonObject.getString("id"));
			}
			return Result.success(list);
		}else if (code == 301) return Result.failure("您的网易cookie已失效，请重新登录！！", null);
		else return Result.failure(jsonObject.getString("msg"), null);
	}

	private JSONArray getSongId(String playListId) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson(api + "/playlist/detail?id=" + playListId);
		return jsonObject.getJSONObject("playlist").getJSONArray("trackIds");
	}

	@Override
	public Result<?> listeningVolume(NetEaseEntity netEaseEntity) throws IOException {
		Result<List<String>> recommend = recommend(netEaseEntity);
		if (recommend.getCode().equals(200)){
			List<String> playList = recommend.getData();
			JSONArray ids = new JSONArray();
			while (ids.size() < 310){
				JSONArray songIds = getSongId(playList.get((int) (Math.random() * playList.size())));
				int k = 0;
				while (ids.size() < 310 && k < songIds.size()){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("download", 0);
					jsonObject.put("end", "playend");
					jsonObject.put("id", songIds.getJSONObject(k).getInteger("id"));
					jsonObject.put("sourceId", "");
					jsonObject.put("time", 240);
					jsonObject.put("type", "song");
					jsonObject.put("wifi", 0);
					JSONObject totalJsonObject = new JSONObject();
					totalJsonObject.put("json", jsonObject);
					totalJsonObject.put("action", "play");
					ids.add(totalJsonObject);
					k++;
				}
			}
			Map<String, String> map = new HashMap<>();
			map.put("logs", ids.toString());
			JSONObject jsonObject = OkHttpUtils.postJson("http://music.163.com/weapi/feedback/weblog",
					prepare(map), OkHttpUtils.addHeaders(netEaseEntity.getCookie(), referer, UA));
			if (jsonObject.getInteger("code").equals(200)) return Result.success("刷每日300听歌量成功！！", null);
			else return Result.failure(jsonObject.getString("message"));
		}else return Result.failure(recommend.getMessage());
	}

	@Override
	public Result<Void> musicianSign(NetEaseEntity netEaseEntity) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson(api + "/musician/tasks",
				OkHttpUtils.addCookie(netEaseEntity.getCookie()));
		if (jsonObject.getInteger("code") != 200) return Result.failure(jsonObject.getString("message"));
		JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
		for (Object o : jsonArray) {
			JSONObject singleJsonObject = (JSONObject) o;
			if (Objects.equals("登录音乐人中心", singleJsonObject.getString("description"))){
				String userMissionId = singleJsonObject.getString("userMissionId");
				String period = singleJsonObject.getString("period");
				JSONObject signJsonObject = OkHttpUtils.getJson(api + "/musician/sign",
						OkHttpUtils.addCookie(netEaseEntity.getCookie()));
				if (signJsonObject.getInteger("code") != 200) return Result.failure("音乐人每日签到失败！" +
						signJsonObject.getString("message"));
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				JSONObject obJsonObject = OkHttpUtils.getJson(api + "/musician/cloudbean/obtain?id=" + userMissionId +
						"&period=" + period, OkHttpUtils.addCookie(netEaseEntity.getCookie()));
				if (obJsonObject.getInteger("code") == 200) return Result.success("网易云音乐人签到成功！", null);
				else return Result.failure("网易云音乐人签到失败！" + obJsonObject.getString("message"));
			}
		}
		return Result.failure("没有找到音乐人签到任务！");
	}
}
