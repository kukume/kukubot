package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ArkNightsEntity;
import me.kuku.yuq.logic.ArkNightsLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.service.ArkNightsService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import org.jsoup.Jsoup;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@JobCenter
public class ArkJob {

	@Inject
	private ArkNightsService arkNightsService;
	@Inject
	private ArkNightsLogic arkNightsLogic;

	@Cron("At::d::05:35")
	public void arkSign(){
		List<ArkNightsEntity> list = arkNightsService.findAll();
		for (ArkNightsEntity arkNightsEntity : list) {
			try {
				String source = "linkShare";
				String uid = "58005820";
				String url = "https://ak.hypergryph.com/activity/preparation?source=linkShare&from=NTgwMDU4MjA=";
				Result<String> cookieResult = arkNightsLogic.akCookie(arkNightsEntity, source, uid);
				if (cookieResult.isFailure()) continue;
				String cookie = arkNightsEntity.getCookie() + cookieResult.getData();
                OkHttpUtils.postJson("https://ak.hypergryph.com/activity/preparation/activity/share",
                        OkHttpUtils.addJson("{\"source\":\"" + source + "\",\"method\":\"" + 1 + "\"}"),
                        OkHttpUtils.addHeaders(cookie, url, UA.PC));
				for (int i = 0; i < 2; i++) {
					OkHttpUtils.postJson("https://ak.hypergryph.com/activity/preparation/activity/roll",
							OkHttpUtils.addJson("{\"source\":\"" + source + "\",\"sourceUid\":\"" + uid + "\"}"),
							OkHttpUtils.addHeaders(cookie, url, UA.PC));
				}
				String html = OkHttpUtils.getStr("https://ak.hypergryph.com/activity/preparation?source=linkShare",
						OkHttpUtils.addUA(UA.PC));
				String js = Jsoup.parse(html).getElementsByTag("script").first().attr("src");
				String jsStr = OkHttpUtils.getStr(js);
				String jsonStr = BotUtils.regex("e.exports=JSON.parse\\('", "'\\)\\},", jsStr);
				JSONObject jsonObject = JSON.parseObject(jsonStr);
				for (Map.Entry<String, Object> entry: jsonObject.entrySet()){
					TimeUnit.MILLISECONDS.sleep(500);
					OkHttpUtils.postJson("https://ak.hypergryph.com/activity/preparation/activity/exchange",
							OkHttpUtils.addJson("{\"giftPackId\":\"" + entry.getKey() + "\",\"source\":\"" + source + "\",\"sourceUid\":\"" + uid + "\"}"),
							OkHttpUtils.addHeaders(cookie, "https://ak.hypergryph.com/activity/preparation?source=linkShare&from=NTgwMDU4MjA=",
									UA.PC));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
