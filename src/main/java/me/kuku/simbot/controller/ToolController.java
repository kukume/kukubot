package me.kuku.simbot.controller;

import catcode.StringTemplate;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.Listen;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import me.kuku.pojo.Result;
import me.kuku.simbot.logic.ToolLogic;
import me.kuku.simbot.utils.BotUtils;
import me.kuku.utils.IOUtils;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@Listen(GroupMsg.class)
public class ToolController {

	@Autowired
	private ToolLogic toolLogic;
	@Autowired
	private MessageContentBuilderFactory messageContentBuilderFactory;
	@Autowired
	private StringTemplate stringTemplate;

	@Filter(value = "{{type,百度|谷歌|bing|搜狗}}{{content}}", matchType = MatchType.REGEX_MATCHES)
	public String teachYou(@FilterValue("content") String content, @FilterValue("type") String type) throws IOException {
		String typeName;
		switch (type){
			case "百度": typeName = "baidu";break;
			case "谷歌": typeName = "google";break;
			case "bing": typeName = "bing";break;
			case "搜狗": typeName = "sougou";break;
			default:return null;
		}
		return toolLogic.teachYou(content, typeName);
	}

	@Filter("舔狗日记")
	public String dogLicking() throws IOException {
		return toolLogic.dogLicking();
	}

	@Filter(value = "百科{{params}}", matchType = MatchType.REGEX_MATCHES)
	public String baiKe(@FilterValue("params") String params) throws IOException {
		return toolLogic.baiKe(params);
	}

	@Filter("毒鸡汤")
	public String poisonousChickenSoup() throws IOException {
		return toolLogic.poisonousChickenSoup();
	}

	@Filter(value = "缩短{{url}}", matchType = MatchType.REGEX_MATCHES)
	public String shortUrl(@FilterValue("url") String url){
		return BotUtils.shortUrl(url);
	}

	@Filter(value = "ip{{params}}", matchType = MatchType.REGEX_MATCHES)
	public String queryIp(@FilterValue("url") String params) throws IOException {
		Result<List<Map<String, String>>> result = toolLogic.queryIp(params);
		if (result.isSuccess()){
			List<Map<String, String>> list = result.getData();
			StringBuilder sb = new StringBuilder();
			list.forEach(map ->
					sb.append(map.get("ip")).append("->").append(map.get("address")).append("\n")
			);
			return MyUtils.removeLastLine(sb);
		}else return "查询IP地址失败！";
	}

	@Filter(value = "whois{{params}}", matchType = MatchType.REGEX_MATCHES)
	public String queryWhois(@FilterValue("params") String params) throws IOException {
		return toolLogic.queryWhois(params);
	}

	@Filter(value = "icp{{params}}", matchType = MatchType.REGEX_MATCHES)
	public String queryIcp(@FilterValue("params") String params) throws IOException {
		return toolLogic.queryIcp(params);
	}

	@Filter("知乎日报")
	public String zhiHuDaily() throws IOException {
		return toolLogic.zhiHuDaily();
	}

	@Filter("测吉凶")
	public String qqGodLock(GroupMsg groupMsg) throws IOException {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		return toolLogic.qqGodLock(qq);
	}

	@Filter(value = "拼音{{params}}", matchType = MatchType.REGEX_MATCHES)
	public String pingYing(@FilterValue("params") String params) throws IOException {
		return toolLogic.convertPinYin(params);
	}

	@Filter("笑话")
	public String jokes() throws IOException {
		return toolLogic.jokes();
	}

	@Filter(value = "垃圾{{params}}", matchType = MatchType.REGEX_MATCHES)
	public String rubbish(@FilterValue("params") String params) throws IOException {
		return toolLogic.rubbish(params);
	}

	@Filter(value = "解析{{url}}", matchType = MatchType.REGEX_MATCHES)
	public String parseVideo(@FilterValue("url")String url) throws IOException {
		return toolLogic.parseVideo(url);
	}

	@Filter(value = "ping{{domain}}", matchType = MatchType.REGEX_MATCHES)
	public String ping(@FilterValue("domain") String domain) throws IOException {
		return toolLogic.ping(domain);
	}

	@Filter(value = "qr{{content}}", matchType = MatchType.REGEX_MATCHES)
	public MessageContent creatQrCode(@FilterValue("content") String content) {
		InputStream is = null;
		try {
			is = toolLogic.creatQr(content);
			return messageContentBuilderFactory.getMessageContentBuilder()
					.image(is).build();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			IOUtils.close(is);
		}
	}

	@Filter("看美女")
	public MessageContent girl() throws IOException {
		return messageContentBuilderFactory.getMessageContentBuilder()
				.image(toolLogic.girlImage()).build();
	}

	@Filter("lol周免")
	public String lolFree() throws IOException {
		return toolLogic.lolFree();
	}

	@Filter(value = "缩写{{content}}", matchType = MatchType.REGEX_MATCHES)
	public String abbreviation(@FilterValue("content") String content) throws IOException {
		return toolLogic.abbreviation(content);
	}

	@Filter(value = "{{bv,^bv.*}}", matchType = MatchType.REGEX_MATCHES)
	@Filter(value = "{{bv,^BV.*}}", matchType = MatchType.REGEX_MATCHES)
	public MessageContent bvToAv(@FilterValue("bv") String bv) throws IOException {
		Result<Map<String, String>> result = toolLogic.bvToAv(bv);
		if (result.getCode() == 200){
			Map<String, String> map = result.getData();
			return messageContentBuilderFactory.getMessageContentBuilder()
					.imageUrl(map.get("pic"))
					.text("标题：" + map.get("title") + "\n" +
							"描述：" + map.get("desc") +
							"链接：" + map.get("url")).build();
		}else return messageContentBuilderFactory.getMessageContentBuilder().text(result.getMessage()).build();
	}

	@Filter("acg")
	public MessageContent acgPic() throws IOException {
		return messageContentBuilderFactory.getMessageContentBuilder().imageUrl(toolLogic.acgPic()).build();
	}

	@Filter(value = "抽象话{{word}}", matchType = MatchType.REGEX_MATCHES)
	public String abstractWords(@FilterValue("word") String word){
		return "抽象话如下：\n" + toolLogic.abstractWords(word);
	}

	@Filter(value = "搜企业{{name}}", matchType = MatchType.REGEX_MATCHES)
	public String searchCompany(@FilterValue("name") String name) throws IOException {
		List<String> list = toolLogic.searchCompany(name);
		StringBuilder sb = new StringBuilder().append("您搜索的企业名称如下：").append("\n");
		list.forEach(s -> sb.append(s).append("\n"));
		return MyUtils.removeLastLine(sb);
	}

	@Filter(value = "查企业{{name}}", matchType = MatchType.REGEX_MATCHES)
	public String queryCompany(@FilterValue("name") String name) throws IOException {
		Map<String, String> map = toolLogic.queryCompanyInfo(name);
		if (map == null) return "没有搜索到该企业！";
		return "企业名称：" + map.get("entName") + "\n" +
				"法定代表人：" + map.get("legalName") + "\n" +
				"注册资本：" + map.get("regCapital") + "\n" +
				"经营状态：" + map.get("regStatus") + "\n" +
				"成立日期：" + map.get("createTime") + "\n" +
				"注册号：" + map.get("regNo") + "\n" +
				"组织机构代码：" + map.get("orgNo") + "\n" +
				"统一社会信用代码：" + map.get("creditCode") + "\n" +
				"核准日期：" + map.get("approvedTime") + "\n" +
				"所属行业：" + map.get("industry") + "\n" +
				"公司类型：" + map.get("companyType") + "\n" +
				"登记机关：" + map.get("regInstitute") + "\n" +
				"所属地区：" + map.get("province") + "\n" +
				"注册地址：" + map.get("regLocation") + "\n" +
				"经营范围：" + map.get("opScope") + "\n" +
				"蓝鲸评分：" + map.get("score");
	}

	@Filter("色图")
	@Filter("色图十连")
	public void seTu(GroupMsg groupMsg, MsgSender msgSender) throws IOException {
		String msg = groupMsg.getMsg();
		int num = "色图十连".equals(msg) ? 10 : 1;
		JSONArray jsonArray = toolLogic.loLiConQuickly(null);
		for (int i = 0 ; i < num; i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String url = jsonObject.getString("quickUrl");
			msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.image(url));
		}
	}
}
