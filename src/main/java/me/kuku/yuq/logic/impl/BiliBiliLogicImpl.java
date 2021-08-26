package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.ResultStatus;
import me.kuku.pojo.UA;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.yuq.entity.BiliBiliEntity;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.pojo.BiliBiliPojo;
import okhttp3.MultipartBody;
import okhttp3.Response;
import okio.ByteString;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BiliBiliLogicImpl implements BiliBiliLogic {
	@Override
	public Result<List<BiliBiliPojo>> getIdByName(String username) throws IOException {
		String enUserName = null;
		try {
			enUserName = URLEncoder.encode(username, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://api.bilibili.com/x/web-interface/search/type?context=&search_type=bili_user&page=1&order=&keyword=%s&category_id=&user_type=&order_sort=&changing=mid&__refresh__=true&_extra=&highlight=1&single_column=0&jsonp=jsonp&callback=__jp2", enUserName),
				OkHttpUtils.addReferer("https://search.bilibili.com/topic?keyword=" + enUserName));
		JSONObject dataJsonObject = jsonObject.getJSONObject("data");
		// pagesize  页大小    numResults 总大小  numPages  页均
		if (dataJsonObject.getInteger("numResults") != 0){
			JSONArray jsonArray = dataJsonObject.getJSONArray("result");
			List<BiliBiliPojo> list = new ArrayList<>();
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject singleJsonObject = jsonArray.getJSONObject(i);
				list.add(new BiliBiliPojo(singleJsonObject.getString("mid"),
						singleJsonObject.getString("uname")));
			}
			return Result.success(list);
		}
		return null;
	}

	private BiliBiliPojo convert(JSONObject jsonObject){
		BiliBiliPojo biliBiliPojo = new BiliBiliPojo();
		JSONObject descJsonObject = jsonObject.getJSONObject("desc");
		JSONObject infoJsonObject = descJsonObject.getJSONObject("user_profile").getJSONObject("info");
		JSONObject forwardJsonObject = descJsonObject.getJSONObject("origin");
		biliBiliPojo.setUserId(infoJsonObject.getString("uid"));
		biliBiliPojo.setName(infoJsonObject.getString("uname"));
		biliBiliPojo.setId(descJsonObject.getString("dynamic_id"));
		biliBiliPojo.setRid(descJsonObject.getString("rid"));
		biliBiliPojo.setTime(Long.parseLong(descJsonObject.getString("timestamp") + "000"));
		biliBiliPojo.setBvId(descJsonObject.getString("bvid"));
		biliBiliPojo.setIsForward(forwardJsonObject != null);
		if (forwardJsonObject != null) {
			biliBiliPojo.setForwardBvId(forwardJsonObject.getString("bvid"));
			Long forwardTime = forwardJsonObject.getLong("timestamp");
			if (forwardTime != null){
				biliBiliPojo.setForwardTime(Long.parseLong(forwardTime + "000"));
			}
			biliBiliPojo.setForwardId(forwardJsonObject.getString("dynamic_id"));
		}
		String cardStr = jsonObject.getString("card");
		JSONObject cardJsonObject = JSON.parseObject(cardStr);
		String text = null;
		if (cardJsonObject != null){
			JSONObject itemJsonObject = cardJsonObject.getJSONObject("item");
			text = cardJsonObject.getString("dynamic");
			List<String> picList = biliBiliPojo.getPicList();
			if (biliBiliPojo.getBvId() != null){
				String picUrl = cardJsonObject.getString("pic");
				picList.add(picUrl);
			}
			if (itemJsonObject != null){
				if (text == null) text = itemJsonObject.getString("description");
				if (text == null) text = itemJsonObject.getString("content");
				JSONArray picJsonArray = itemJsonObject.getJSONArray("pictures");
				if (picJsonArray != null) {
					picJsonArray.forEach(obj -> {
						JSONObject picJsonObject = (JSONObject) obj;
						picList.add(picJsonObject.getString("img_src"));
					});
				}
			}
			if (text == null) {
				JSONObject vestJsonObject = cardJsonObject.getJSONObject("vest");
				if (vestJsonObject != null){
					text = vestJsonObject.getString("content");
				}
			}
			if (text == null && cardJsonObject.containsKey("title")){
				text = cardJsonObject.getString("title") + "------" + cardJsonObject.getString("summary");
			}
			String originStr = cardJsonObject.getString("origin");
			if (originStr != null){
				List<String> forwardPicList = biliBiliPojo.getForwardPicList();
				JSONObject forwardContentJsonObject = JSON.parseObject(originStr);
				if (biliBiliPojo.getForwardBvId() != null){
					String picUrl = forwardContentJsonObject.getString("pic");
					forwardPicList.add(picUrl);
				}
				if (forwardContentJsonObject.containsKey("item")){
					JSONObject forwardItemJsonObject = forwardContentJsonObject.getJSONObject("item");
					biliBiliPojo.setForwardText(forwardItemJsonObject.getString("description"));
					if (biliBiliPojo.getForwardText() == null){
						biliBiliPojo.setForwardText(forwardItemJsonObject.getString("content"));
					}
					JSONArray forwardPicJsonArray = forwardItemJsonObject.getJSONArray("pictures");
					if (forwardPicJsonArray != null){
						for (Object obj : forwardPicJsonArray) {
							JSONObject picJsonObject = (JSONObject) obj;
							forwardPicList.add(picJsonObject.getString("img_src"));
						}
					}
					JSONObject forwardUserJsonObject = forwardContentJsonObject.getJSONObject("user");
					if (forwardUserJsonObject != null) {
						biliBiliPojo.setForwardUserId(forwardUserJsonObject.getString("uid"));
						biliBiliPojo.setForwardName(forwardUserJsonObject.getString("name"));
						if (biliBiliPojo.getForwardName() == null) {
							biliBiliPojo.setForwardName(forwardUserJsonObject.getString("uname"));
						}
					}else {
						JSONObject forwardOwnerJsonObject = forwardContentJsonObject.getJSONObject("owner");
						if (forwardOwnerJsonObject != null){
							biliBiliPojo.setForwardUserId(forwardOwnerJsonObject.getString("mid"));
							biliBiliPojo.setForwardName(forwardOwnerJsonObject.getString("name"));
						}
					}
				}else {
					biliBiliPojo.setForwardText(forwardContentJsonObject.getString("dynamic"));
					JSONObject forwardOwnerJsonObject = forwardContentJsonObject.getJSONObject("owner");
					if (forwardOwnerJsonObject != null){
						biliBiliPojo.setForwardUserId(forwardOwnerJsonObject.getString("mid"));
						biliBiliPojo.setForwardName(forwardOwnerJsonObject.getString("name"));
					}else {
						biliBiliPojo.setForwardName(forwardContentJsonObject.getString("uname"));
						biliBiliPojo.setForwardUserId(forwardContentJsonObject.getString("uid"));
						biliBiliPojo.setForwardText(forwardContentJsonObject.getString("title"));
					}
				}
			}
		}
		if (text == null) text = "没有发现内容";
		biliBiliPojo.setText(text);
		int type;
		if (biliBiliPojo.getBvId() == null){
			if (biliBiliPojo.getPicList() == null) type = 17;
			else type = 11;
		}else type = 1;
		biliBiliPojo.setType(type);
		return biliBiliPojo;
	}

	@Override
	public String convertStr(BiliBiliPojo biliBiliPojo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String bvId = biliBiliPojo.getBvId();
		String forwardBvId = biliBiliPojo.getForwardBvId();
		String line = "\n";
		StringBuilder sb = new StringBuilder()
				.append(biliBiliPojo.getName()).append(line)
				.append("发布时间：").append(sdf.format(new Date(biliBiliPojo.getTime()))).append(line)
				.append("内容：").append(biliBiliPojo.getText()).append(line)
				.append("动态链接：").append(String.format("https://t.bilibili.com/%s", biliBiliPojo.getId())).append(line)
				.append("视频链接：");
		if (bvId != null) sb.append(String.format("https://www.bilibili.com/video/%s", bvId));
		else sb.append("没有发现视频");
		if (biliBiliPojo.getIsForward()){
			sb.append(line).append("转发自:").append(biliBiliPojo.getForwardName()).append(line)
					.append("发布时间：").append(sdf.format(new Date(biliBiliPojo.getForwardTime()))).append(line)
					.append("内容：").append(biliBiliPojo.getForwardText()).append(line)
					.append("动态链接：");
			if (forwardBvId != null) sb.append("https://www.bilibili.com/video/").append(forwardBvId);
			else sb.append("没有发现视频");
		}
		return sb.toString();
	}

	private Result<List<BiliBiliPojo>> getDynamicById(String id, String offsetId) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=%s&offset_dynamic_id=%s&need_top=1", id, offsetId),
				OkHttpUtils.addReferer("https://space.bilibili.com/$id/dynamic"));
		// next_offset  下一页开头
		JSONObject dataJsonObject = jsonObject.getJSONObject("data");
		JSONArray jsonArray = dataJsonObject.getJSONArray("cards");
		if (jsonArray == null) return Result.failure(ResultStatus.DYNAMIC_NOT_FOUNT);
		List<BiliBiliPojo> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			JSONObject extraJsonObject = singleJsonObject.getJSONObject("extra");
			if (extraJsonObject != null && Integer.valueOf(1).equals(extraJsonObject.getInteger("is_space_top"))) continue;
			list.add(convert(singleJsonObject));
		}
		return Result.success(dataJsonObject.getString("next_offset"), list);
	}

	@Override
	public Result<List<BiliBiliPojo>> getDynamicById(String id) throws IOException {
		return getDynamicById(id, "0");
	}

	@Override
	public List<BiliBiliPojo> getAllDynamicById(String id) throws IOException {
		String offsetId = "0";
		List<BiliBiliPojo> allList = new ArrayList<>();
		while (true){
			Result<List<BiliBiliPojo>> result = getDynamicById(id, offsetId);
			if (result.getCode().equals(200)){
				List<BiliBiliPojo> list = result.getData();
				allList.addAll(list);
				offsetId = result.getMessage();
			}else return allList;
		}
	}

	@Override
	public String loginByQr1() throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://passport.bilibili.com/qrcode/getLoginUrl");
		return jsonObject.getJSONObject("data").getString("url");
	}

	@Override
	public Result<BiliBiliEntity> loginByQr2(String url) throws IOException {
		String oauthKey = MyUtils.regex("(?<=oauthKey\\=).*", url);
		if (oauthKey == null) return Result.failure("链接格式不正确！！", null);
		Map<String, String> map = new HashMap<>();
		map.put("oauthKey", oauthKey);
		map.put("gourl", "https://www.bilibili.com");
		JSONObject jsonObject = OkHttpUtils.postJson("https://passport.bilibili.com/qrcode/getLoginInfo", map);
		Boolean status = jsonObject.getBoolean("status");
		if (!status){
			switch (jsonObject.getInteger("data")){
				case -2: return Result.failure("您的二维码已过期！！", null);
				case -4: return Result.failure(ResultStatus.QRCODE_NOT_SCANNED);
				case -5: return Result.failure(ResultStatus.QRCODE_IS_SCANNED);
				default: return Result.failure(jsonObject.getString("message"), null);
			}
		}else {
			String successUrl = jsonObject.getJSONObject("data").getString("url");
			Response response = OkHttpUtils.get(successUrl, OkHttpUtils.addReferer("https://passport.bilibili.com/login"));
			return Result.success(getBiliBiliEntityByResponse(response));
		}
	}

	private BiliBiliEntity getBiliBiliEntityByResponse(Response response){
		response.close();
		String cookie = OkHttpUtils.getCookie(response);
		String token = MyUtils.regex("bili_jct=", "; ", cookie);
		if (token == null) return null;
		String locationUrl = response.header("location");
		String userId = MyUtils.regex("DedeUserID=", "&", locationUrl);
		return BiliBiliEntity.Companion.getInstance(cookie, userId, token);
	}

	@Override
	public Result<List<BiliBiliPojo>> getFriendDynamic(BiliBiliEntity biliBiliEntity) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new?type_list=268435455",
				OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (!jsonObject.getInteger("code").equals(0)) return Result.failure(ResultStatus.COOKIE_EXPIRED);
		JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("cards");
		List<BiliBiliPojo> list = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++){
			list.add(convert(jsonArray.getJSONObject(i)));
		}
		return Result.success(list);
	}

	@Override
	public JSONObject live(String id) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=" + id,
				OkHttpUtils.addReferer("https://api.live.bilibili.com"));
		JSONObject dataJsonObject = jsonObject.getJSONObject("data");
		int status = dataJsonObject.getInteger("liveStatus");
		JSONObject resultJsonObject = new JSONObject();
		resultJsonObject.put("status", status == 1);
		resultJsonObject.put("title", dataJsonObject.getString("title"));
		resultJsonObject.put("url", dataJsonObject.getString("url"));
		return resultJsonObject;
	}

	@Override
	public String liveSign(BiliBiliEntity biliBiliEntity) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign",
				OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (jsonObject.getInteger("code").equals(0)) return "哔哩哔哩直播签到成功！！";
		else return jsonObject.getString("message");
	}

	@Override
	public String like(BiliBiliEntity biliBiliEntity, String id, Boolean isLike) throws IOException {
		Map<String, String> map = new HashMap<>();
		map.put("uid", biliBiliEntity.getUserid());
		map.put("dynamic_id", id);
		int up = 2;
		if (isLike) up = 1;
		map.put("up", String.valueOf(up));
		map.put("csrf_token", biliBiliEntity.getToken());
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.vc.bilibili.com/dynamic_like/v1/dynamic_like/thumb", map,
				OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (jsonObject.getInteger("code").equals(0)) return "赞动态成功";
		else return "赞动态失败，" + jsonObject.getString("message");
	}

	@Override
	public String comment(BiliBiliEntity biliBiliEntity, String rid, String type, String content) throws IOException {
		HashMap<String, String> map = new HashMap<>();
		map.put("oid", rid);
		map.put("type", type);
		map.put("message", content);
		map.put("plat", "1");
		map.put("jsonp", "jsonp");
		map.put("csrf", biliBiliEntity.getToken());
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.bilibili.com/x/v2/reply/add", map, OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (jsonObject.getInteger("code").equals(0)) return "评论动态成功！！";
		else return "评论动态失败，" + jsonObject.getString("message");
	}

	@Override
	public String forward(BiliBiliEntity biliBiliEntity, String id, String content) throws IOException {
		HashMap<String, String> map = new HashMap<>();
		map.put("uid", biliBiliEntity.getUserid());
		map.put("dynamic_id", id);
		map.put("content", content);
		map.put("extension", "{\"emoji_type\":1}");
		map.put("at_uids", "");
		map.put("ctrl", "[]");
		map.put("csrf_token", biliBiliEntity.getToken());
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/repost", map,
				OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (jsonObject.getInteger("code") == 0) return "转发动态成功！！";
		else return "转发动态失败，" + jsonObject.getString("message");
	}

	@Override
	public String tossCoin(BiliBiliEntity biliBiliEntity, String rid, int count) throws IOException {
		HashMap<String, String> map = new HashMap<>();
		map.put("aid", rid);
		map.put("multiply", String.valueOf(count));
		map.put("select_like", "1");
		map.put("cross_domain", "true");
		map.put("csrf", biliBiliEntity.getToken());
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.bilibili.com/x/web-interface/coin/add", map,
				OkHttpUtils.addHeader().add("cookie", biliBiliEntity.getCookie())
						.add("referer", "https://www.bilibili.com/video/").build());
		if (jsonObject.getInteger("code").equals(0)) return "对该动态（视频）投硬币成功！！";
		else return "对该动态（视频）投硬币失败！！，" + jsonObject.getString("message");
	}

	@Override
	public String favorites(BiliBiliEntity biliBiliEntity, String rid, String name) throws IOException {
		String userId = biliBiliEntity.getUserid();
		String cookie = biliBiliEntity.getCookie();
		String token = biliBiliEntity.getToken();
		JSONObject firstJsonObject = OkHttpUtils.getJson(String.format("https://api.bilibili.com/x/v3/fav/folder/created/list-all?type=2&rid=%s&up_mid=%s", rid, userId),
				OkHttpUtils.addCookie(cookie));
		if (firstJsonObject.getInteger("code") != 0) return "收藏失败，请重新绑定哔哩哔哩！！";
		AtomicReference<String> favoriteId = new AtomicReference<>();
		JSONArray jsonArray = firstJsonObject.getJSONObject("data").getJSONArray("list");
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			if (jsonObject.getString("title").equals(name)){
				favoriteId.set(jsonObject.getString("id"));
			}
		}
		if (favoriteId.get() == null){
			HashMap<String, String> map = new HashMap<>();
			map.put("title", name);
			map.put("privacy", "0");
			map.put("jsonp", "jsonp");
			map.put("csrf", token);
			JSONObject jsonObject = OkHttpUtils.postJson("https://api.bilibili.com/x/v3/fav/folder/add", map,
					OkHttpUtils.addCookie(cookie));
			if (jsonObject.getInteger("code") != 0) return "您并没有该收藏夹，而且创建该收藏夹失败，请重试！！";
			favoriteId.set(jsonObject.getJSONObject("data").getString("id"));
		}
		HashMap<String, String> map = new HashMap<>();
		map.put("rid", rid);
		map.put("type", "2");
		map.put("add_media_ids", favoriteId.get());
		map.put("del_media_ids", "");
		map.put("jsonp", "jsonp");
		map.put("csrf", token);
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.bilibili.com/x/v3/fav/resource/deal", map,
				OkHttpUtils.addCookie(cookie));
		if (jsonObject.getInteger("code").equals(0)) return "收藏该视频成功！！";
		else return "收藏视频失败，" + jsonObject.getString("message");
	}

	@Override
	public Result<JSONObject> uploadImage(BiliBiliEntity biliBiliEntity, ByteString byteString) throws IOException {
		MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("file_up", "123.jpg", OkHttpUtils.addStream(byteString))
				.addFormDataPart("biz", "draw")
				.addFormDataPart("category", "daily").build();
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.vc.bilibili.com/api/v1/drawImage/upload", body,
				OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (jsonObject.getInteger("code") == 0) return Result.success(jsonObject.getJSONObject("data"));
		else return Result.failure("图片上传失败，" + jsonObject.getString("message"), null);
	}

	@Override
	public String publishDynamic(BiliBiliEntity biliBiliEntity, String content, List<String> images) throws IOException {
		JSONArray jsonArray = new JSONArray();
		for (String url : images) {
			jsonArray.add(uploadImage(biliBiliEntity, OkHttpUtils.getByteStr(url)));
		}
		HashMap<String, String> map = new HashMap<>();
		map.put("biz", "3");
		map.put("category", "3");
		map.put("type", "0");
		map.put("pictures", jsonArray.toString());
		map.put("title", "");
		map.put("tags", "");
		map.put("description", content);
		map.put("content", content);
		map.put("setting", "{\"copy_forbidden\":0,\"cachedTime\":0}");
		map.put("from", "create.dynamic.web");
		map.put("up_choose_comment", "0");
		map.put("extension", "{\"emoji_type\":1,\"from\":{\"emoji_type\":1},\"flag_cfg\":{}}");
		map.put("at_uids", "");
		map.put("at_control", "");
		map.put("csrf_token", biliBiliEntity.getToken());
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/create_draw", map,
				OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (jsonObject.getInteger("code") == 0) return "发布动态成功！！";
		else return "发布动态失败，" + jsonObject.getString("message");
	}

	@Override
	public List<Map<String, String>> getRanking() throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://api.bilibili.com/x/web-interface/ranking/v2?rid=0&type=all");
		JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
		ArrayList<Map<String, String>> list = new ArrayList<>();
		for (Object obj: jsonArray){
			JSONObject singleJsonObject = (JSONObject) obj;
			HashMap<String, String> map = new HashMap<>();
			map.put("aid", singleJsonObject.getString("aid"));
			map.put("cid", singleJsonObject.getString("cid"));
			map.put("title", singleJsonObject.getString("title"));
			map.put("desc", singleJsonObject.getString("desc"));
			map.put("username", singleJsonObject.getJSONObject("owner").getString("name"));
			map.put("dynamic", singleJsonObject.getString("dynamic"));
			map.put("bv", singleJsonObject.getString("bvid"));
			list.add(map);
		}
		return list;
	}

	@Override
	public String report(BiliBiliEntity biliBiliEntity, String aid, String cid, int proGRes) throws IOException {
		HashMap<String, String> map = new HashMap<>();
		map.put("aid", aid);
		map.put("cid", cid);
		map.put("progres", String.valueOf(proGRes));
		map.put("csrf", biliBiliEntity.getToken());
		JSONObject jsonObject = OkHttpUtils.postJson("http://api.bilibili.com/x/v2/history/report", map,
				OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (jsonObject.getInteger("code").equals(0)) return "模拟观看视频成功！！";
		else return jsonObject.getString("message");
	}

	@Override
	public String share(BiliBiliEntity biliBiliEntity, String aid) throws IOException {
		HashMap<String, String> map = new HashMap<>();
		map.put("aid", aid);
		map.put("csrf", biliBiliEntity.getToken());
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.bilibili.com/x/web-interface/share/add", map,
				OkHttpUtils.addCookie(biliBiliEntity.getCookie()));
		if (jsonObject.getInteger("code").equals(0)) return "分享视频成功！！";
		else return jsonObject.getString("message");
	}

	@Override
	public List<Map<String, String>> getReplay(BiliBiliEntity biliBiliEntity, String oid, int page) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJsonp("https://api.bilibili.com/x/v2/reply?callback=jQuery17207366906764958399_" + System.currentTimeMillis() + "&jsonp=jsonp&pn=" + page + "&type=1&oid=" + oid + "&sort=2&_=" + System.currentTimeMillis(),
				OkHttpUtils.addHeaders(biliBiliEntity.getCookie(), "https://www.bilibili.com/"));
		if (jsonObject.getInteger("code") == 0){
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("replies");
			List<Map<String, String>> list = new ArrayList<>();
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject singleJsonObject = jsonArray.getJSONObject(i);
				Map<String, String> map = new HashMap<>();
				map.put("id", singleJsonObject.getString("rpid"));
				map.put("content", singleJsonObject.getJSONObject("content").getString("message"));
				list.add(map);
			}
			return list;
		}else return null;
	}

	@Override
	public String reportComment(BiliBiliEntity biliBiliEntity, String oid, String rpId, int reason) throws IOException {
		// 违法违规 9   色情  2    低俗 10    赌博诈骗  12
		// 人身攻击  7   侵犯隐私 15
		// 垃圾广告 1   引战 4    剧透   5    刷屏   3      抢楼 16    内容不相关   8     青少年不良信息  17
		//  其他 0
		Map<String, String> map = new HashMap<>();
		map.put("oid", oid);
		map.put("type", "1");
		map.put("rpid", rpId);
		map.put("reason", String.valueOf(reason));
		map.put("content", "");
		map.put("ordering", "heat");
		map.put("jsonp", "jsonp");
		map.put("csrf", biliBiliEntity.getToken());
		JSONObject jsonObject = OkHttpUtils.postJson("https://api.bilibili.com/x/v2/reply/report", map,
				OkHttpUtils.addHeaders(biliBiliEntity.getCookie(), "https://www.bilibili.com/"));
		if (jsonObject.getInteger("code") == 0) return "举报评论成功！！";
		else return "举报评论失败！！";
	}

	@Override
	public String getOidByBvId(String bvId) throws IOException {
		String html = OkHttpUtils.getStr("https://www.bilibili.com/video/" + bvId,
				OkHttpUtils.addUA(UA.PC));
		String jsonStr = MyUtils.regex("INITIAL_STATE__=", ";\\(function\\(\\)", html);
		JSONObject jsonObject = JSON.parseObject(jsonStr);
		return jsonObject.getString("aid");
	}

	@Override
	public Result<List<Map<String, String>>> followed(BiliBiliEntity biliBiliEntity) throws IOException {
		Map<String, String> headers = new HashMap<>();
		headers.put("referer", "https://space.bilibili.com/" + biliBiliEntity.getUserid() + "/fans/follow");
		headers.put("user-agent", UA.PC.getValue());
		headers.put("cookie", biliBiliEntity.getCookie());
		JSONObject jsonObject = OkHttpUtils.getJsonp(
				String.format("https://api.bilibili.com/x/relation/followings?vmid=%s&pn=%s&ps=%s&order=desc&order_type=attention&jsonp=jsonp&callback=__jp5",
						biliBiliEntity.getUserid(), "1", "20"), OkHttpUtils.addHeaders(headers));
		if (jsonObject.getInteger("code") == 0){
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
			List<Map<String, String>> list = new ArrayList<>();
			jsonArray.stream().map(it -> (JSONObject) it).forEach(it -> {
				Map<String, String> map = new HashMap<>();
				map.put("id", it.getString("mid"));
				map.put("name", it.getString("uname"));
				list.add(map);
			});
			return Result.success(list);
		}else return Result.failure(jsonObject.getString("message"));
	}
}
