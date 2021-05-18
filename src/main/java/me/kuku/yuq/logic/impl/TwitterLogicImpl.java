package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.logic.TwitterLogic;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.TwitterPojo;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Headers;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TwitterLogicImpl implements TwitterLogic {

	@Inject
	private ConfigService configService;

	private Headers addHeaders(){
		ConfigEntity configEntity = configService.findByType(ConfigType.TWITTER_COOKIE);
		String authorization = "";
		if (configEntity != null) authorization = configEntity.getContent();
		String token = "";
		try {
			String html = OkHttpUtils.getStr("https://twitter.com/explore");
			token = BotUtils.regex("gt=", "; ", html);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Headers.Builder()
				.add("authorization", authorization)
				.add("x-guest-token", token)
				.build();
	}

	@Override
	public List<TwitterPojo> findIdByName(String name) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://twitter.com/i/api/2/search/adaptive.json?include_profile_interstitial_type=1&include_blocking=1&include_blocked_by=1&include_followed_by=1&include_want_retweets=1&include_mute_edge=1&include_can_dm=1&include_can_media_tag=1&skip_status=1&cards_platform=Web-12&include_cards=1&include_ext_alt_text=true&include_quote_count=true&include_reply_count=1&tweet_mode=extended&include_entities=true&include_user_entities=true&include_ext_media_color=true&include_ext_media_availability=true&send_error_codes=true&simple_quoted_tweet=true&q="+ URLEncoder.encode(name, "utf-8") + "&result_filter=user&count=20&query_source=typed_query&pc=1&spelling_corrections=1&ext=mediaStats%2ChighlightedLabel",
				addHeaders());
		JSONObject usersJsonObject = jsonObject.getJSONObject("globalObjects").getJSONObject("users");
		List<TwitterPojo> list = new ArrayList<>();
		for (Map.Entry<String, Object> entry: usersJsonObject.entrySet()){
			long id = Long.parseLong(entry.getKey());
			JSONObject singleJsonObject = (JSONObject) entry.getValue();
			String username = singleJsonObject.getString("name");
			String screenName = singleJsonObject.getString("screen_name");
			list.add(new TwitterPojo(id, username, screenName));
		}
		return list;
	}

	@Override
	public List<TwitterPojo> findTweetsById(Long id) throws IOException {
		JSONObject jsonObject = OkHttpUtils.getJson("https://api.twitter.com/2/timeline/profile/" + id + ".json?include_profile_interstitial_type=1&include_blocking=1&include_blocked_by=1&include_followed_by=1&include_want_retweets=1&include_mute_edge=1&include_can_dm=1&include_can_media_tag=1&skip_status=1&cards_platform=Web-12&include_cards=1&include_ext_alt_text=true&include_quote_count=true&include_reply_count=1&tweet_mode=extended&include_entities=true&include_user_entities=true&include_ext_media_color=true&include_ext_media_availability=true&send_error_codes=true&simple_quoted_tweet=true&include_tweet_replies=false&count=20&userId=" + id + "&ext=mediaStats%2ChighlightedLabel",
				addHeaders());
//		JSONArray entriesJsonArray = jsonObject.getJSONObject("timeline").getJSONArray("instructions").getJSONObject(0)
//				.getJSONObject("addEntries").getJSONArray("entries");
//		JSONObject entriesJsonObject = entriesJsonArray.getJSONObject(entriesJsonArray.size() - 1);
//		String cursor = entriesJsonObject.getJSONObject("content").getJSONObject("operation").getJSONObject("cursor").getString("value");
		JSONObject tweetsJsonObject = jsonObject.getJSONObject("globalObjects").getJSONObject("tweets");
		JSONObject userJsonObject = (JSONObject) jsonObject.getJSONObject("globalObjects").getJSONObject("users").values().iterator().next();
		String name = userJsonObject.getString("name");
		String screenName = userJsonObject.getString("screen_name");
		List<TwitterPojo> list = new ArrayList<>();
		for (Map.Entry<String, Object> entry: tweetsJsonObject.entrySet()){
			long tweetsId = Long.parseLong(entry.getKey());
			JSONObject singleJsonObject = (JSONObject) entry.getValue();
			String createdAt = singleJsonObject.getString("created_at");
			String text = singleJsonObject.getString("full_text");
			List<String> picList = new ArrayList<>();
			JSONObject extendedJsonObject = singleJsonObject.getJSONObject("extended_entities");
			if (extendedJsonObject != null) {
				JSONArray mediaJsonArray = extendedJsonObject.getJSONArray("media");
				for (int i = 0; i < mediaJsonArray.size(); i++) {
					JSONObject mediaSingleJsonJsonObject = mediaJsonArray.getJSONObject(i);
					if ("photo".equals(mediaSingleJsonJsonObject.getString("type"))) {
						String url = mediaSingleJsonJsonObject.getString("media_url_https");
						picList.add(url);
					}
				}
			}
			String url = "https://twitter.com/" + screenName + "/status/" + tweetsId;
			list.add(new TwitterPojo(id, name, screenName, tweetsId, createdAt, text, url, picList));
		}
		list.sort(TwitterPojo::compareTo);
		return list;
	}
}
