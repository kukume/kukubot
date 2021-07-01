package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.UA;
import me.kuku.utils.MD5Utils;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.RSAUtils;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.logic.DdOcrCodeLogic;
import me.kuku.yuq.logic.MiHoYoLogic;
import me.kuku.yuq.pojo.DdOcrPojo;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class MiHoYoLogicImpl implements MiHoYoLogic {
	@Inject
	private DdOcrCodeLogic ddOcrCodeLogic;

	private final String version = "2.3.0";

	@Override
	public Result<MiHoYoEntity> login(String account, String password) throws IOException {
		JSONObject beforeJsonObject = OkHttpUtils.getJson("https://webapi.account.mihoyo.com/Api/create_mmt?scene_type=1&now=" +
				System.currentTimeMillis() + "&reason=bbs.mihoyo.com", OkHttpUtils.addUA(UA.PC));
		JSONObject dataJsonObject = beforeJsonObject.getJSONObject("data").getJSONObject("mmt_data");
		String challenge = dataJsonObject.getString("challenge");
		String gt = dataJsonObject.getString("gt");
		String mmtKey = dataJsonObject.getString("mmt_key");
		Result<DdOcrPojo> identifyResult = ddOcrCodeLogic.identify(gt, challenge, "https://bbs.mihoyo.com/ys/");
		if (identifyResult.isFailure()) return Result.failure(identifyResult.getMessage());
		DdOcrPojo ddOcrPojo = identifyResult.getData();
		String rsaKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDvekdPMHN3AYhm/vktJT+YJr7cI5DcsNKqdsx5DZX0gDuWFuIjzdwButrIYPNmRJ1G8ybDIF7oDW2eEpm5sMbL9zs9ExXCdvqrn51qELbqj0XxtMTIpaCHFSI50PfPpTFV9Xt/hmyVwokoOXFlAEgCn+QCgGs52bFoYMtyi+xEQIDAQAB";
		String enPassword;
		try {
			enPassword = RSAUtils.encrypt(password, RSAUtils.getPublicKey(rsaKey));
		} catch (Exception e) {
			return Result.failure("密码加密失败，请重试！！");
		}
		Map<String, String> map = new HashMap<>();
		map.put("is_bh2", "false");
		map.put("account", account);
		map.put("password", enPassword);
		map.put("mmt_key", mmtKey);
		map.put("is_crypto", "true");
		map.put("geetest_challenge", ddOcrPojo.getChallenge());
		map.put("geetest_validate", ddOcrPojo.getValidate());
		map.put("geetest_seccode", ddOcrPojo.getSecCode());
		Response response = OkHttpUtils.post("https://webapi.account.mihoyo.com/Api/login_by_password",
				map, OkHttpUtils.addUA(UA.PC));
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		JSONObject infoDataJsonObject = jsonObject.getJSONObject("data");
		if (infoDataJsonObject.getInteger("status") != 1) return Result.failure(infoDataJsonObject.getString("msg"));
		String cookie = OkHttpUtils.getCookie(response);
		JSONObject infoJsonObject = infoDataJsonObject.getJSONObject("account_info");
		String accountId = infoJsonObject.getString("account_id");
		String ticket = infoJsonObject.getString("weblogin_token");
		JSONObject cookieJsonObject = OkHttpUtils.getJson("https://webapi.account.mihoyo.com/Api/cookie_accountinfo_by_loginticket?login_ticket=" +
				ticket + "&t=" + System.currentTimeMillis(), OkHttpUtils.addHeaders(cookie, "", UA.PC));
		String cookieToken = cookieJsonObject.getJSONObject("data").getJSONObject("cookie_info").getString("cookie_token");
		cookie += "cookie_token=" + cookieToken + "; account_id=" + accountId + "; ";
		Response loginResponse = OkHttpUtils.post("https://bbs-api.mihoyo.com/user/wapi/login",
				OkHttpUtils.addJson("{\"gids\":\"2\"}"), OkHttpUtils.addCookie(cookie));
		loginResponse.close();
		String finaCookie = OkHttpUtils.getCookie(loginResponse);
		cookie += finaCookie;
		MiHoYoEntity miHoYoEntity = new MiHoYoEntity(null, null, account, password, cookie, accountId, ticket, cookieToken);
		return Result.success(miHoYoEntity);
	}

	private String ds(String n){
//		String n = "h8w582wxwgqvahcdkpvdhbh2w9casgfl";
//		String n = "pbcfcvnfsm5s2w4x3lsq8caor7v8nlqm";
		String i = String.valueOf(System.currentTimeMillis() / 1000);
		String r = MyUtils.randomStrLetter(6);
		String c = MD5Utils.toMD5("salt=" + n + "&t=" + i + "&r=" + r);
		return i + "," + r + "," + c;
	}

	private String ds(){
		return ds("h8w582wxwgqvahcdkpvdhbh2w9casgfl");
	}

	private Map<String, String> headerMap(MiHoYoEntity miHoYoEntity){
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("DS", ds());
		headerMap.put("x-rpc-app_version", version);
		headerMap.put("x-rpc-client_type", "5");
		headerMap.put("x-rpc-device_id", UUID.randomUUID().toString());
		headerMap.put("user-agent", "Mozilla/5.0 (Linux; Android 10; V1914A Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/88.0.4324.181 Mobile Safari/537.36 miHoYoBBS/2.5.1");
		headerMap.put("Referer", "https://webstatic.mihoyo.com/bbs/event/signin-ys/index.html?bbs_auth_required=true&act_id=e202009291139501&utm_source=bbs&utm_medium=mys&utm_campaign=icon");
		headerMap.put("cookie", miHoYoEntity.getCookie());
		return headerMap;
	}

	private Result<List<Long>> genShinRoleId(MiHoYoEntity miHoYoEntity) throws IOException {
		JSONObject ssJsonObject = OkHttpUtils.getJson("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=hk4e_cn",
				OkHttpUtils.addCookie(miHoYoEntity.getCookie()));
		if (ssJsonObject.getInteger("retcode") != 0) return Result.failure(ssJsonObject.getString("message"));
		JSONArray jsonArray = ssJsonObject.getJSONObject("data").getJSONArray("list");
		if (jsonArray.size() == 0) return Result.failure("您还没有原神角色！！");
		List<Long> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			String uid = singleJsonObject.getString("game_uid");
			list.add(Long.parseLong(uid));
		}
		return Result.success(list);
	}

	@Override
	public String sign(MiHoYoEntity miHoYoEntity) throws IOException {
		JSONObject ssJsonObject = OkHttpUtils.getJson("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=hk4e_cn",
				OkHttpUtils.addCookie(miHoYoEntity.getCookie()));
		if (ssJsonObject.getInteger("retcode") != 0) return ssJsonObject.getString("message");
		JSONArray jsonArray = ssJsonObject.getJSONObject("data").getJSONArray("list");
		if (jsonArray.size() == 0) return "您还没有原神角色！！";
		JSONObject jsonObject = null;
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			jsonObject = OkHttpUtils.postJson("https://api-takumi.mihoyo.com/event/bbs_sign_reward/sign",
					OkHttpUtils.addJson("{\"act_id\":\"e202009291139501\",\"region\":\"cn_gf01\",\"uid\":\"" +
							singleJsonObject.getString("game_uid") + "\"}"),
					OkHttpUtils.addHeaders(headerMap(miHoYoEntity)));
		}
		if (jsonObject.getInteger("retcode") == 0) return "签到成功！！";
		else return jsonObject.getString("message");
	}

	/*@Override
	public String bbsSign(MiHoYoEntity miHoYoEntity) throws IOException {
		Headers headers = OkHttpUtils.addHeaders(headerMap(miHoYoEntity));
		JSONObject signJsonObject = OkHttpUtils.postJson("https://bbs-api.mihoyo.com/apihub/sapi/signIn",
				OkHttpUtils.addJson("{\"gids\":\"2\"}"), headers);
		if (signJsonObject.getInteger("retcode") == 0){
			return "讨论区签到成功；";
		}else return "讨论区签到失败，" + signJsonObject.getString("message");
	}

	@Override
	public String bbsPost(MiHoYoEntity miHoYoEntity, String id) throws IOException {
		JSONObject postJsonObject = OkHttpUtils.getJson("https://bbs-api.mihoyo.com/post/api/getPostFull?post_id=" + id,
				OkHttpUtils.addHeaders(headerMap(miHoYoEntity)));
		if (postJsonObject.getInteger("retcode") == 0){
			return "浏览帖子成功；";
		}else return "浏览帖子失败，" + postJsonObject.getString("message");
	}

	@Override
	public String bbsLike(MiHoYoEntity miHoYoEntity, String id) throws IOException {
		JSONObject jsonObject = OkHttpUtils.postJson("https://bbs-api.mihoyo.com/apihub/sapi/upvotePost",
				OkHttpUtils.addJson("{\"is_cancel\":false,\"post_id\":\"" + id + "\"}"),
				OkHttpUtils.addHeaders(headerMap(miHoYoEntity)));
		if (jsonObject.getInteger("retcode") == 0){
			return "点赞帖子成功；";
		}else return "点赞帖子失败，" + jsonObject.getString("message");
	}

	@Override
	public String bbsShare(MiHoYoEntity miHoYoEntity, String id) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://bbs-api.mihoyo.com/apihub/api/getShareConf?entity_id=" + id
						+ "&entity_type=1",
				OkHttpUtils.addHeaders(headerMap(miHoYoEntity)));
		if (jsonObject.getInteger("retcode") == 0){
			return "分享帖子成功；";
		}else return "分享帖子失败，" + jsonObject.getString("message");
	}*/

	@Override
	public String genShinUserInfo(MiHoYoEntity miHoYoEntity, Long id) throws IOException {
		if (id == null){
			Result<List<Long>> roleResult = genShinRoleId(miHoYoEntity);
			if (roleResult.isFailure()) return roleResult.getMessage();
			id = roleResult.getData().get(0);
		}
		Map<String, String> map = new HashMap<>();
		map.put("DS", ds("pbcfcvnfsm5s2w4x3lsq8caor7v8nlqm"));
		map.put("x-rpc-app_version", "2.4.0");
		map.put("User-Agent", "Mozilla/5.0 (Linux; Android 9; Unspecified Device) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/39.0.0.0 Mobile Safari/537.36 miHoYoBBS/2.2.");
		map.put("x-rpc-client_type", "5");
		map.put("Referer", "https://webstatic.mihoyo.com/app/community-game-records/index.html?v=6");
		map.put("X-Requested-With", "com.mihoyo.hyperion");
		map.put("cookie", miHoYoEntity.getCookie());
		// cn_gf01 官服  cn_qd01 B服
		JSONObject jsonObject = OkHttpUtils.getJson("https://api-takumi.mihoyo.com/game_record/genshin/api/index?server=cn_gf01&role_id=" + id,
				map);
		if (jsonObject.getInteger("retcode") != 0) return jsonObject.getString("message");
		StringBuilder sb = new StringBuilder(id + " Genshin Info:\nRoles:\n");
		JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("avatars");
		for (Object obj: jsonArray){
			JSONObject singleJsonObject = (JSONObject) obj;
			String element = singleJsonObject.getString("element");
			String type;
			switch (element){
				case "None": type = "无属性"; break;
				case "Anemo": type = "风属性"; break;
				case "Pyro": type = "火属性"; break;
				case "Geo": type = "岩属性"; break;
				case "Electro": type = "雷属性"; break;
				case "Cryo": type = "冰属性"; break;
				case "Hydro": type = "水属性"; break;
				default: type = "草属性";
			}
			String name = singleJsonObject.getString("name");
			String text = "";
			if ("旅行者".equals(name)){
				String image = singleJsonObject.getString("image");
				text += "* " + name + "：\n";
				if (image.contains("UI_AvatarIcon_PlayerGirl")){
					text += "  - [萤——妹妹] " + singleJsonObject.getString("level") + "级 " + type + "\n";
				}else if (image.contains("UI_AvatarIcon_PlayerBoy")){
					text += "  - [空——哥哥] " + singleJsonObject.getString("level") + "级 " + type + "\n";
				}else{
					text += "  - [性别判断失败] " + singleJsonObject.getString("level") + "级 " + type + "\n";
				}
			}else{
				text += "* " + singleJsonObject.getString("name") + " " + singleJsonObject.getString("rarity") +
						"★角色:\n";
				text += "  - " + singleJsonObject.getString("level") + "级 好感度(" + singleJsonObject.getString("fetter") +
						")级 " + type + "\n";
			}
			sb.append(text);
		}
		JSONObject statsJsonObject = jsonObject.getJSONObject("data").getJSONObject("stats");
		sb.append("\nAccount Info:\n");
		sb.append("- 活跃天数：").append(statsJsonObject.getString("active_day_number")).append(" 天\n");
		sb.append("- 达成成就：").append(statsJsonObject.getString("achievement_number")).append(" 个\n");
		sb.append("- 获得角色：").append(statsJsonObject.getString("avatar_number")).append(" 个\n");
		sb.append("- 深渊螺旋：");
		if ("-".equals(statsJsonObject.getString("spiral_abyss"))){
			sb.append("没打");
		}else sb.append("打到了").append(statsJsonObject.getString("spiral_abyss"));
		sb.append("\n").append("* 收集：\n");
		sb.append("  - 风神瞳").append(statsJsonObject.getString("anemoculus_number")).append(" 个 岩神瞳")
				.append(statsJsonObject.getString("geoculus_number")).append("个\n");
		sb.append("* 解锁：\n");
		sb.append("  - 传送点").append(statsJsonObject.getString("way_point_number")).append("个 秘境")
				.append(statsJsonObject.getString("domain_number")).append("个\n");
		sb.append("* 共开启宝箱：\n");
		sb.append("  - 普通：").append(statsJsonObject.getString("common_chest_number")).append("个 精致：")
				.append(statsJsonObject.getString("exquisite_chest_number")).append("个\n")
				.append("  - 珍贵：").append(statsJsonObject.getString("luxurious_chest_number")).append("个 华丽：")
				.append(statsJsonObject.getString("precious_chest_number")).append("个");
		return sb.toString();
	}
}
