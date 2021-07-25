package me.kuku.simbot.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.UA;
import me.kuku.simbot.entity.WeiboEntity;
import me.kuku.simbot.logic.WeiboLogic;
import me.kuku.simbot.pojo.WeiboPojo;
import me.kuku.simbot.pojo.WeiboToken;
import me.kuku.utils.HexUtils;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.RSAUtils;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Service
public class WeiboLogicImpl implements WeiboLogic {
	@Override
	public List<String> hotSearch() throws IOException {
		Document doc = Jsoup.connect("https://s.weibo.com/top/summary").get();
		Elements elements = doc.getElementById("pl_top_realtimehot").getElementsByTag("tbody").first()
				.getElementsByTag("tr");
		List<String> list = new ArrayList<>();
		for (Element ele: elements){
			String text = ele.getElementsByClass("td-01").first().text();
			if ("".equals(text)) text = "顶";
			String title = ele.getElementsByClass("td-02").first().getElementsByTag("a").first().text();
			list.add(text + "、" + title);
		}
		return list;
	}

	@Override
	public Result<List<WeiboPojo>> getIdByName(String name) throws IOException {
		String newName = URLEncoder.encode(name, "utf-8");
		Response response = OkHttpUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D3%26q%3D" + newName + "%26t%3D0&page_type=searchall",
				OkHttpUtils.addReferer("https://m.weibo.cn/search?containerid=100103type%3D1%26q%3D" + newName));
		if (response.code() == 200){
			JSONObject jsonObject = OkHttpUtils.getJson(response);
			JSONArray cardsJsonArray = jsonObject.getJSONObject("data").getJSONArray("cards");
			JSONArray jsonArray = null;
			for (Object obj: cardsJsonArray){
				JSONObject singleJsonObject = (JSONObject) obj;
				JSONArray cardGroupJsonArray = singleJsonObject.getJSONArray("card_group");
				if (cardGroupJsonArray != null){
					jsonArray = cardGroupJsonArray;
					break;
				}
			}
			if (jsonArray == null) return Result.failure("没有找到该用户！！", null);
			List<WeiboPojo> list = new ArrayList<>();
			for (Object obj: jsonArray){
				JSONObject newJsonObject = (JSONObject) obj;
				if (newJsonObject.containsKey("user") || newJsonObject.containsKey("users")) {
					JSONObject userJsonObject = newJsonObject.getJSONObject("user");
					if (userJsonObject != null) {
						String username = userJsonObject.getString("name");
						if (username == null) username = userJsonObject.getString("screen_name");
						list.add(new WeiboPojo(username, userJsonObject.getString("id")));
					}else {
						JSONArray usersJsonArray = newJsonObject.getJSONArray("users");
						for (int i = 0; i < usersJsonArray.size(); i++){
							JSONObject singleJsonObject = usersJsonArray.getJSONObject(i);
							String username = singleJsonObject.getString("name");
							if (username == null) username = singleJsonObject.getString("screen_name");
							list.add(new WeiboPojo(username, singleJsonObject.getString("id")));
						}
					}
				}
			}
			if (list.size() == 0) return Result.failure("未找到该用户", null);
			else return Result.success(list);
		}else return Result.failure("查询失败，请稍后再试！！", null);
	}

	private WeiboPojo convert(JSONObject jsonObject){
		WeiboPojo weiboPojo = new WeiboPojo();
		JSONObject userJsonObject = jsonObject.getJSONObject("user");
		weiboPojo.setId(Long.parseLong(jsonObject.getString("id")));
		weiboPojo.setName(userJsonObject.getString("screen_name"));
		weiboPojo.setCreated(jsonObject.getString("created_at"));
		weiboPojo.setText(Jsoup.parse(jsonObject.getString("text")).text());
		weiboPojo.setBid(jsonObject.getString("bid"));
		weiboPojo.setUserId(userJsonObject.getString("id"));
		Integer picNum = jsonObject.getInteger("pic_num");
		if (picNum != 0){
			List<String> list = new ArrayList<>();
			JSONArray jsonArray = jsonObject.getJSONArray("pics");
			if (jsonArray != null){
				jsonArray.forEach(obj -> {
					JSONObject picJsonObject = (JSONObject) obj;
					String url = picJsonObject.getJSONObject("large").getString("url");
					list.add(url);
				});
			}
			weiboPojo.setImageUrl(list);
		}
		if (jsonObject.containsKey("retweeted_status")){
			JSONObject forwardJsonObject = jsonObject.getJSONObject("retweeted_status");
			weiboPojo.setIsForward(true);
			weiboPojo.setForwardId(forwardJsonObject.getString("id"));
			weiboPojo.setForwardTime(forwardJsonObject.getString("created_at"));
			JSONObject forwardUserJsonObject = forwardJsonObject.getJSONObject("user");
			String name = null;
			if (forwardUserJsonObject == null) name = "原微博已删除";
			else forwardUserJsonObject.getString("screen_name");
			weiboPojo.setForwardName(name);
			weiboPojo.setForwardText(Jsoup.parse(forwardJsonObject.getString("text")).text());
			weiboPojo.setForwardBid(forwardJsonObject.getString("bid"));
		}else weiboPojo.setIsForward(false);
		return weiboPojo;
	}

	@Override
	public String convertStr(WeiboPojo weiboPojo) {
		StringBuilder sb = new StringBuilder();
		sb.append(weiboPojo.getName()).append("\n")
				.append("发布时间：").append(weiboPojo.getCreated()).append("\n")
				.append("内容：").append(weiboPojo.getText()).append("\n")
				.append("链接：").append("https://m.weibo.cn/status/").append(weiboPojo.getBid());
		if (weiboPojo.getIsForward()){
			sb.append("\n")
					.append("转发自：").append(weiboPojo.getForwardName()).append("\n")
					.append("发布时间：").append(weiboPojo.getForwardTime()).append("\n")
					.append("内容：").append(weiboPojo.getForwardText()).append("\n")
					.append("链接：").append("https://m.weibo.cn/status/").append(weiboPojo.getForwardBid());
		}
		return sb.toString();
	}

	@Override
	public Result<List<WeiboPojo>> getWeiboById(String id) throws IOException {
		Response response = OkHttpUtils.get(String.format("https://m.weibo.cn/api/container/getIndex?type=uid&uid=%s&containerid=107603%s", id, id));
		if (response.code() == 200){
			JSONObject jsonObject = OkHttpUtils.getJson(response);
			JSONArray cardJsonArray = jsonObject.getJSONObject("data").getJSONArray("cards");
			List<WeiboPojo> list = new ArrayList<>();
			for (Object o : cardJsonArray) {
				JSONObject singleJsonObject = (JSONObject) o;
				JSONObject blogJsonObject = singleJsonObject.getJSONObject("mblog");
				if (blogJsonObject == null) continue;
				if (Integer.valueOf(1).equals(blogJsonObject.getInteger("isTop"))) continue;
				WeiboPojo weiboPojo = convert(blogJsonObject);
				list.add(weiboPojo);
			}
			return Result.success(list);
		}else return Result.failure("查询失败，请稍后再试！！", null);
	}

	@Override
	public String getCaptchaUrl(String pcId){
		return "https://login.sina.com.cn/cgi/pin.php?r=" + MyUtils.randomNum(8) + "&s=0&p=" + pcId;
	}

	private String encryptPassword(Map<String, String> map, String password){
		String message =  map.get("servertime") + "\t" + map.get("nonce") + "\n" + password;
		try {
			password = RSAUtils.encrypt(message, RSAUtils.getPublicKey(map.get("pubkey"), "10001"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] bytes = Base64.getDecoder().decode(password);
		return HexUtils.bytesToHexString(bytes);
	}

	private String getMobileCookie(String pcCookie) throws IOException {
		Response response = OkHttpUtils.get("https://login.sina.com.cn/sso/login.php?url=https%3A%2F%2Fm.weibo.cn%2F%3F%26jumpfrom%3Dweibocom&_rand=1588483688.7261&gateway=1&service=sinawap&entry=sinawap&useticket=1&returntype=META&sudaref=&_client_version=0.6.33",
				OkHttpUtils.addCookie(pcCookie));
		response.close();
		return OkHttpUtils.getCookie(response);
	}

	@Override
	public Result<Map<String, String>> login(Map<String, String> map, String door) throws IOException {
		String newPassword = encryptPassword(map, map.get("password"));
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("entry", "weibo");
		paramMap.put("gateway", "1");
		paramMap.put("from", "");
		paramMap.put("savestate", "7");
		paramMap.put("qrcode_flag", "false");
		paramMap.put("useticket", "1");
		paramMap.put("pagerefer", "https://passport.weibo.com/");
		if (door != null) {
			String pcId = map.get("pcid");
			paramMap.put("pcid", pcId);
			paramMap.put("door", door);
		}
		paramMap.put("vsnf", "1");
		paramMap.put("su", map.get("username"));
		paramMap.put("service", "miniblog");
		paramMap.put("servertime", map.get("servertime"));
		paramMap.put("nonce", map.get("nonce"));
		paramMap.put("pwencode", "rsa2");
		paramMap.put("rsakv", map.get("rsakv"));
		paramMap.put("sp", newPassword);
		paramMap.put("sr", "1536*864");
		paramMap.put("encoding", "UTF-8");
		paramMap.put("prelt", "189");
		paramMap.put("url", "https://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack");
		paramMap.put("returntype", "META");
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("user-agent", UA.PC.getValue());
		headerMap.put("referer", "https://weibo.com/");
		headerMap.put("Origin", "https://weibo.com");
		Response response = OkHttpUtils.post("https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.19)&_=" + System.currentTimeMillis(),
				paramMap, OkHttpUtils.addHeaders(headerMap));
		String html = OkHttpUtils.getStr(response);
		String url = MyUtils.regex("location.replace\\(\"", "\"\\);", html);
		if (url == null) return Result.failure("获取失败！！", null);
		String token = MyUtils.regex("token%3D", "\"\\);", html);
		String cookie = OkHttpUtils.getCookie(response);
		map.put("cookie", cookie);
		if (url.contains("https://login.sina.com.cn/crossdomain2.php")){
			map.put("url", url);
			map.put("referer", "https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.19)");
			return Result.success(map);
		}else if (token == null){
			String reason = MyUtils.regex("reason=", "&", html);
			String result = URLDecoder.decode(reason, "gbk");
			return Result.failure(result, null);
		}else {
			map.put("token", token);
			return Result.failure(201, "账号需要验证！！", map);
		}
	}

	@Override
	public Result<Map<String, String>> loginByMobile(String username, String password) throws IOException {
		Map<String, String> map = new HashMap<>();
		map.put("username", username);
		map.put("password", password);
		map.put("savestate", "1");
		map.put("r", "https://m.weibo.cn/?jumpfrom=weibocom");
		map.put("ec", "0");
		map.put("pagerefer", "https://m.weibo.cn/login?backURL=https%3A%2F%2Fm.weibo.cn%2F%3Fjumpfrom%3Dweibocom");
		map.put("entry", "mweibo");
		map.put("wentry", "");
		map.put("loginfrom", "");
		map.put("client_id", "");
		map.put("code", "");
		map.put("qq", "");
		map.put("mainpageflag", "1");
		map.put("hff", "");
		map.put("hfp", "");
		JSONObject jsonObject = OkHttpUtils.postJson("https://passport.weibo.cn/sso/login", map,
				OkHttpUtils.addReferer("https://passport.weibo.cn/signin/login?entry=mweibo&res=wel&wm=3349&r=https%3A%2F%2Fm.weibo.cn%2F%3Fjumpfrom%3Dweibocom"));
		Integer code = jsonObject.getInteger("retcode");
		if (code == 50050011){
			JSONObject data = jsonObject.getJSONObject("data");
			String errUrl = data.getString("errurl");
			String id = MyUtils.regex("id=", "&", errUrl);
			if (id == null){
				return Result.failure("登录失败，请稍后再试！！");
			}else {
				Map<String, String> resultMap = new HashMap<>();
				String cookie = "FID=" + id + "; ";
				String html = OkHttpUtils.getStr("https://passport.weibo.cn/signin/secondverify/index?first_enter=1&c=",
						OkHttpUtils.addHeaders(cookie, "", UA.PC));
				String phone = MyUtils.regex("\"maskMobile\":\"", "\"", html);
				resultMap.put("cookie", cookie);
				resultMap.put("phone", phone);
				return Result.failure(201, "账号需要验证", resultMap);
			}
		}else {
			return Result.failure("");
		}
	}

	@Override
	public Result<String> loginByMobileSms1(String phone, String cookie) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://passport.weibo.cn/signin/secondverify/ajsend?number=1&mask_mobile=" + phone +
				"&msg_type=sms", OkHttpUtils.addCookie(cookie));
		if (jsonObject.getInteger("retcode") == 100000){
			return Result.success("发送成功", null);
		}else return Result.failure(jsonObject.getString("msg"));
	}

	@Override
	public Result<WeiboEntity> loginByMobileSms2(String code, String cookie) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://passport.weibo.cn/signin/secondverify/ajcheck?msg_type=sms&code=" + code,
				OkHttpUtils.addCookie(cookie));
		if (jsonObject.getInteger("retcode") == 100000){
			String firstUrl = jsonObject.getJSONObject("data").getString("url");
			WeiboEntity weiboEntity = loginSuccess(firstUrl);
			return Result.success(weiboEntity);
		}else return Result.failure(jsonObject.getString("msg"));
	}

	@Override
	public Result<String> loginByMobilePrivateMsg1(String cookie) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://passport.weibo.cn/signin/secondverify/ajsend?msg_type=private_msg",
				OkHttpUtils.addCookie(cookie));
		if (jsonObject.getInteger("retcode") == 100000){
			return Result.success("发送成功！！", null);
		}else return Result.failure(jsonObject.getString("msg"));
	}

	@Override
	public Result<WeiboEntity> loginByMobilePrivateMsg2(String code, String cookie) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://passport.weibo.cn/signin/secondverify/ajcheck?msg_type=private_msg&code=" + code,
				OkHttpUtils.addCookie(cookie));
		if (jsonObject.getInteger("retcode") == 100000){
			String url = jsonObject.getJSONObject("data").getString("url");
			WeiboEntity weiboEntity = loginSuccess(url);
			return Result.success(weiboEntity);
		}return Result.failure(jsonObject.getString("msg"));
	}

	private WeiboEntity loginSuccess(String url) throws IOException {
		Response firstResponse = OkHttpUtils.get(url);
		firstResponse.close();
		String secondUrl = firstResponse.header("location");
		Response secondResponse = OkHttpUtils.get(secondUrl);
		secondResponse.close();
		String thirdUrl = secondResponse.header("location");
		Response thirdResponse = OkHttpUtils.get(thirdUrl);
		thirdResponse.close();
		String pcCookie = OkHttpUtils.getCookie(thirdResponse);
		String forthUrl = thirdResponse.header("location");
		Response forthResponse = OkHttpUtils.get(forthUrl);
		forthResponse.close();
		String mobileCookie = OkHttpUtils.getCookie(forthResponse);
		return WeiboEntity.Companion.getInstance(pcCookie, mobileCookie);
	}

	@Override
	public Map<String, String> loginByQr1() throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJsonp("https://login.sina.com.cn/sso/qrcode/image?entry=weibo&size=180&callback=STK_16010457545441",
				OkHttpUtils.addReferer("https://weibo.com/"));
		jsonObject = jsonObject.getJSONObject("data");
		Map<String, String> map = new HashMap<>();
		map.put("id", jsonObject.getString("qrid"));
		map.put("url", jsonObject.getString("image"));
		return map;
	}

	@Override
	public Result<WeiboEntity> loginByQr2(String id) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJsonp("https://login.sina.com.cn/sso/qrcode/check?entry=weibo&qrid=" + id + "&callback=STK_16010457545443",
				OkHttpUtils.addReferer("https://weibo.com/"));
		switch (jsonObject.getInteger("retcode")){
			case 20000000:
				JSONObject dataJsonObject = jsonObject.getJSONObject("data");
				String alt = dataJsonObject.getString("alt");
				Response response = OkHttpUtils.get("https://login.sina.com.cn/sso/login.php?entry=weibo&returntype=TEXT&crossdomain=1&cdult=3&domain=weibo.com&alt=" + alt + "&savestate=30&callback=STK_160104719639113");
				String cookie = OkHttpUtils.getCookie(response);
				jsonObject = OkHttpUtils.getJsonp(response);
				assert jsonObject != null;
				JSONArray jsonArray = jsonObject.getJSONArray("crossDomainUrlList");
				String url = jsonArray.getString(2);
				Response finallyResponse = OkHttpUtils.get(url);
				finallyResponse.close();
				String pcCookie = OkHttpUtils.getCookie(finallyResponse);
				String mobileCookie = getMobileCookie(cookie);
				return Result.success(WeiboEntity.Companion.getInstance(pcCookie, mobileCookie));
			case 50114001: return Result.failure(201, "未扫码！！");
			case 50114003: return Result.failure("您的微博登录二维码已失效！！", null);
			case 50114002: return Result.failure(202, "已扫码！！");
			default: return Result.failure(jsonObject.getString("msg"), null);
		}
	}

	@Override
	public Result<List<WeiboPojo>> getFriendWeibo(WeiboEntity weiboEntity) throws IOException {
		String str = OkHttpUtils.getStr("https://m.weibo.cn/feed/friends?",
				OkHttpUtils.addCookie(weiboEntity.getMobileCookie()));
		if (!"".equals(str)){
			JSONArray jsonArray;
			try {
				jsonArray = JSON.parseObject(str).getJSONObject("data").getJSONArray("statuses");
			}catch (JSONException e){
				return Result.failure("查询微博失败，请稍后再试！！", null);
			}
			List<WeiboPojo> list = new ArrayList<>();
			for (Object o : jsonArray) {
				JSONObject jsonObject = (JSONObject) o;
				WeiboPojo weiboPojo = convert(jsonObject);
				list.add(weiboPojo);
			}
			return Result.success(list);
		}else return Result.failure("您的cookie已失效，请重新绑定微博！！", null);
	}

	@Override
	public Result<List<WeiboPojo>> getMyWeibo(WeiboEntity weiboEntity) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://m.weibo.cn/profile/info",
				OkHttpUtils.addCookie(weiboEntity.getMobileCookie()));
		if (jsonObject.getInteger("ok") == 1){
			JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("statuses");
			List<WeiboPojo> list = new ArrayList<>();
			for (Object obj : jsonArray) {
				JSONObject singleJsonObject = (JSONObject) obj;
				list.add(convert(singleJsonObject));
			}
			if (list.size() == 0) return Result.failure("没有发现微博！！", null);
			else return Result.success(list);
		}else return Result.failure(jsonObject.getString("msg"), null);
	}

	@Override
	public Result<List<WeiboPojo>> weiboTopic(String keyword) throws IOException {
		Response response = OkHttpUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D1%26q%3D%23" + URLEncoder.encode(keyword, "utf-8") + "%23&page_type=searchall");
		if (response.code() != 200) return Result.failure("查询失败，请稍后再试！！", null);
		JSONObject jsonObject = OkHttpUtils.getJson(response);
		JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("cards");
		List<WeiboPojo> list = new ArrayList<>();
		for (Object obj : jsonArray) {
			JSONObject singleJsonObject = (JSONObject) obj;
			JSONObject mBlogJsonObject = singleJsonObject.getJSONObject("mblog");
			if (mBlogJsonObject != null) list.add(convert(mBlogJsonObject));
		}
		if (list.size() == 0) return Result.failure("没有找到该话题", null);
		else return Result.success(list);
	}

	Result<WeiboToken> getToken(WeiboEntity weiboEntity) throws IOException {
		Response response = OkHttpUtils.get("https://m.weibo.cn/api/config",
				OkHttpUtils.addCookie(weiboEntity.getMobileCookie()));
		JSONObject jsonObject = OkHttpUtils.getJson(response).getJSONObject("data");
		if (jsonObject.getBoolean("login")){
			String cookie = OkHttpUtils.getCookie(response);
			return Result.success(new WeiboToken(jsonObject.getString("st"),
					cookie + weiboEntity.getMobileCookie()));
		}else return Result.failure("登录已失效，", null);
	}

	@Override
	public String like(WeiboEntity weiboEntity, String id) throws IOException {
		WeiboToken weiboToken = getToken(weiboEntity).getData();
		if (weiboToken == null) return "登录已失效";
		Map<String, String> map = new HashMap<>();
		map.put("id", id);
		map.put("attitude", "heart");
		map.put("st", weiboToken.getToken());
		map.put("_spr", "screen:1536x864");
		JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/attitudes/create",
				map, OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/detail/" + id));
		return jsonObject.getString("msg");
	}

	@Override
	public String comment(WeiboEntity weiboEntity, String id, String commentContent) throws IOException {
		WeiboToken weiboToken = getToken(weiboEntity).getData();
		if (weiboToken == null) return "登录已失效！！";
		Map<String, String> map = new HashMap<>();
		map.put("content", commentContent);
		map.put("mid", id);
		map.put("st", weiboToken.getToken());
		map.put("_spr", "screen:411x731");
		JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/comments/create",
				map, OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/detail/" + id));
		if (jsonObject.getInteger("ok") == 1) return "评论成功！！";
		else return jsonObject.getString("msg");
	}

	private String uploadPic(String picUrl, String referer, WeiboToken weiboToken) throws IOException {
		MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("type", "json")
				.addFormDataPart("pic", "pic.jpg", OkHttpUtils.addStream(picUrl))
				.addFormDataPart("st", weiboToken.getToken())
				.addFormDataPart("_spr", "screen:411x731").build();
		JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/statuses/uploadPic",
				body, OkHttpUtils.addHeaders(weiboToken.getCookie(), referer));
		return jsonObject.getString("pic_id");
	}

	@Override
	public String forward(WeiboEntity weiboEntity, String id, String content, String picUrl) throws IOException {
		WeiboToken weiboToken = getToken(weiboEntity).getData();
		if (weiboToken == null) return "登录已失效";
		String picId = null;
		if (picUrl != null){
			picId = uploadPic(picUrl, "https://m.weibo.cn/compose/repost?id=" + id, weiboToken);
		}
		FormBody.Builder builder = new FormBody.Builder()
				.add("id", id)
				.add("content", content)
				.add("mid", id)
				.add("st", weiboToken.getToken())
				.add("_spr", "screen:411x731");
		if (picId != null) builder.add("picId", picId);
		else picId = "";
		JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/statuses/repost", builder.build(),
				OkHttpUtils.addHeaders(weiboToken.getCookie(),
						"https://m.weibo.cn/compose/repost?id=" + id + "&pids=" + picId));
		if (jsonObject.getInteger("ok") == 1) return "转发微博成功！！";
		else return jsonObject.getString("msg");
	}

	@Override
	public String getUserInfo(String id) throws IOException {
		Response response = OkHttpUtils.get("https://m.weibo.cn/api/container/getIndex?uid=" + id + "&luicode=10000011&lfid=100103type%3D1&containerid=100505" + id);
		if (response.code() == 200){
			JSONObject jsonObject = OkHttpUtils.getJson(response);
			JSONObject userInfoJsonObject = jsonObject.getJSONObject("data").getJSONObject("userInfo");
			return "id：" + userInfoJsonObject.getString("id") + "\n" +
					"昵称：" + userInfoJsonObject.getString("screen_name") + "\n" +
					"关注：" + userInfoJsonObject.getString("follow_count") + "\n" +
					"粉丝：" + userInfoJsonObject.getString("followers_count") + "\n" +
					"微博会员：" + userInfoJsonObject.getString("mbrank") + "级\n" +
					"微博认证：" + userInfoJsonObject.getString("verified_reason") + "\n" +
					"描述：" + userInfoJsonObject.getString("description") + "\n" +
					"主页：" + "https://m.weibo.cn/u/" + userInfoJsonObject.getString("id");
		}else return "查询失败，请稍后再试！！！";
	}

	@Override
	public String publishWeibo(WeiboEntity weiboEntity, String content, List<String> url) throws IOException {
		WeiboToken weiboToken = getToken(weiboEntity).getData();
		if (weiboToken == null) return "登录已失效";
		StringBuilder picIds = new StringBuilder();
		if (url != null){
			url.forEach(str -> {
				try {
					String id = uploadPic(str, "https://m.weibo.cn/compose/", weiboToken);
					picIds.append(id).append(",");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		FormBody.Builder builder = new FormBody.Builder()
				.add("content", content)
				.add("st", weiboToken.getToken())
				.add("_spr", "screen:411x731");
		StringBuilder newSb = picIds.deleteCharAt(picIds.length() - 1);
		if (picIds.length() != 0){
			builder.add("picId", newSb.toString());
		}
		JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/statuses/update", builder.build(),
				OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/compose/?pids=" + newSb));
		if (jsonObject.getInteger("ok") == 1) return "发布微博成功！！";
		else return jsonObject.getString("msg");
	}

	@Override
	public String removeWeibo(WeiboEntity weiboEntity, String id) throws IOException {
		WeiboToken weiboToken = getToken(weiboEntity).getData();
		if (weiboToken == null) return "登录已失效";
		Map<String, String> map = new HashMap<>();
		map.put("mid", id);
		map.put("st", weiboToken.getToken());
		map.put("_spr", "screen:411x731");
		JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/profile/delMyblog",
				map, OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/profile/"));
		if (jsonObject.getInteger("ok") == 1) return "删除微博成功！！";
		else return jsonObject.getString("msg");
	}

	@Override
	public String favoritesWeibo(WeiboEntity weiboEntity, String id) throws IOException {
		WeiboToken weiboToken = getToken(weiboEntity).getData();
		if (weiboToken == null) return "登录已失效";
		Map<String, String> map = new HashMap<>();
		map.put("id", id);
		map.put("st", weiboToken.getToken());
		map.put("_spr", "screen:411x731");
		JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/mblogDeal/addFavMblog",
				map, OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/"));
		return jsonObject.getString("msg");
	}

	@Override
	public String shortUrl(WeiboEntity weiboEntity, String url) throws IOException {
		WeiboToken weiboToken = getToken(weiboEntity).getData();
		if (weiboToken == null) return "登录已失效";
		Map<String, String> map = new HashMap<>();
		if (!url.startsWith("http")) url = "http://" + url;
		map.put("content", url);
		map.put("st", weiboToken.getToken());
		map.put("_spr", "screen:1536x864");
		JSONObject jsonObject = OkHttpUtils.postJson("https://m.weibo.cn/api/statuses/update", map,
				OkHttpUtils.addHeaders(weiboToken.getCookie(), "https://m.weibo.cn/compose/"));
		if (jsonObject.getInteger("ok") == 1){
			JSONObject dataJsonObject = jsonObject.getJSONObject("data");
			String id = dataJsonObject.getString("id");
			String content = dataJsonObject.getString("text");
			String shortUrl = Jsoup.parse(content).getElementsByTag("a").first().attr("href");
			removeWeibo(weiboEntity, id);
			return shortUrl;
		}else return "获取短链接失败！！";
	}
}
