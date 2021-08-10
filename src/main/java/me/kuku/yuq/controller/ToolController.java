package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.IceCreamQAQ.Yu.util.IO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.job.RainInfo;
import com.icecreamqaq.yuq.message.Image;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.Result;
import me.kuku.utils.DateTimeFormatterUtils;
import me.kuku.utils.IOUtils;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.CodeType;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
@GroupController
public class ToolController {
	@Inject
	private ToolLogic toolLogic;
	@Inject
	private MessageItemFactory mif;
	@Inject
	private MessageService messageService;
	@Inject
	private GroupService groupService;
	@Inject
	private JobManager jobManager;
	@Inject
	private RainInfo rainInfo;
	@Config("YuQ.Mirai.bot.master")
	private String master;
	@Inject
	private ConfigService configService;

	@Action("百度 {content}")
	@Synonym({"谷歌 {content}", "bing {content}", "搜狗 {content}"})
	@QMsg(at = true)
	public String teachYou(String content, @PathVar(0) String type) throws IOException {
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

	@Action("舔狗日记")
	@QMsg(at = true)
	public String dogLicking() throws IOException {
		return toolLogic.dogLicking();
	}

	@Action("百科 {params}")
	@QMsg(at = true)
	public String baiKe(String params) throws IOException {
		return toolLogic.baiKe(params);
	}

	@Action("毒鸡汤")
	@QMsg(at = true)
	public String poisonousChickenSoup() throws IOException {
		return toolLogic.poisonousChickenSoup();
	}

	@Action("名言")
	@QMsg(at = true)
	public String saying() throws IOException {
		return toolLogic.saying();
	}

	@Action("一言")
	@QMsg(at = true)
	public String hiToKoTo() throws IOException {
		return toolLogic.hiToKoTo().get("text");
	}

	@Action(value = "缩短 {url}")
	@QMsg(at = true)
	public String shortUrl(String url){
		return BotUtils.shortUrl(url);
	}

	@Action(value = "ip {params}")
	@QMsg(at = true)
	public String queryIp(String params) throws IOException {
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

	@Action("whois {params}")
	@QMsg(at = true)
	public String queryWhois(String params) throws IOException {
		return toolLogic.queryWhois(params);
	}

	@Action("icp {params}")
	@QMsg(at = true)
	public String queryIcp(String params) throws IOException {
		return toolLogic.queryIcp(params);
	}

	@Action("知乎日报")
	@QMsg(at = true)
	public String zhiHuDaily() throws IOException {
		return toolLogic.zhiHuDaily();
	}

	@Action("测吉凶")
	@QMsg(at = true)
	public String qqGodLock(long qq) throws IOException {
		return toolLogic.qqGodLock(qq);
	}

	@Action("拼音 {params}")
	@QMsg(at = true)
	public String pingYing(String params) throws IOException {
		return toolLogic.convertPinYin(params);
	}

	@Action("笑话")
	@QMsg(at = true)
	public String jokes() throws IOException {
		return toolLogic.jokes();
	}

	@Action(value = "垃圾 {params}")
	@QMsg(at = true)
	public String rubbish(String params) throws IOException {
		return toolLogic.rubbish(params);
	}

	@Action(value = "解析 {url}")
	@QMsg(at = true)
	public String parseVideo(String url) throws IOException {
		return toolLogic.parseVideo(url);
	}

	@Action(value = "ping {domain}")
	@QMsg(at = true)
	public String ping(String domain) throws IOException {
		return toolLogic.ping(domain);
	}

	@Action(value = "qr {content}")
	@QMsg(at = true)
	public Message creatQrCode(String content) {
		InputStream is = null;
		try {
			is = toolLogic.creatQr(content);
			return mif.imageByInputStream(is).toMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			IOUtils.close(is);
		}
	}

	@Action("看美女")
	@QMsg(at = true)
	public Image girl() throws IOException {
		return mif.imageByUrl(toolLogic.girlImage());
	}

	@Action("lol周免")
	@QMsg(at = true)
	public String lolFree() throws IOException {
		return toolLogic.lolFree();
	}

	@Action(value = "缩写 {content}")
	@QMsg(at = true)
	public String abbreviation(String content) throws IOException {
		return toolLogic.abbreviation(content);
	}

	@Action(value = "\\^bv.*\\")
	@Synonym(value = "\\^BV.*\\")
	public Message bvToAv(@PathVar(0) String bv) throws IOException {
		Result<Map<String, String>> result = toolLogic.bvToAv(bv);
		if (result.getCode() == 200){
			Map<String, String> map = result.getData();
			return mif.imageByUrl(map.get("pic"))
					.plus("标题：" + map.get("title") + "\n" +
							"描述：" + map.get("desc") +
							"链接：" + map.get("url"));
		}else return mif.text(result.getMessage()).toMessage();
	}

	@Action("acg")
	public Image acgPic() throws IOException {
		return mif.imageByUrl(toolLogic.acgPic());
	}

	@Action("分词 {word}")
	@QMsg(at = true)
	public String wordSegmentation(long qq, String word) throws IOException {
		return toolLogic.wordSegmentation(word);
	}

	@Action(value = "抽象话 {word}")
	@QMsg(at = true)
	public String abstractWords(String word){
		return "抽象话如下：\n" + toolLogic.abstractWords(word);
	}

	@Action(value = "搜企业 {name}")
	@QMsg(at = true)
	public String searchCompany(String name) throws IOException {
		List<String> list = toolLogic.searchCompany(name);
		StringBuilder sb = new StringBuilder().append("您搜索的企业名称如下：").append("\n");
		list.forEach(s -> sb.append(s).append("\n"));
		return MyUtils.removeLastLine(sb);
	}

	@Action(value = "查企业 {name}")
	@QMsg(at = true)
	public String queryCompany(String name) throws IOException {
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

	@Action("色图")
	@Synonym("色图十连")
	public void seTu(GroupEntity groupEntity, Message message, Group group, long qq) throws IOException {
		if (Boolean.TRUE.equals(groupEntity.getColorPic())) {
			String msg = Message.Companion.firstString(message);
			int num = "色图十连".equals(msg) ? 10 : 1;
			JSONArray jsonArray = toolLogic.loLiConQuickly(null);
			for (int i = 0; i < num; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String url = jsonObject.getString("quickUrl");
				group.sendMessage(mif.imageByUrl(url).toMessage());
			}
		}else group.sendMessage(mif.at(qq).plus("色图功能已关闭！"));
	}

	@Action("查发言数")
	@QMsg(at = true)
	public String queryMessage(Group group, GroupEntity groupEntity, Member qq){
		String today = DateTimeFormatterUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");
		long time = DateTimeFormatterUtils.parseDate(today, "yyyy-MM-dd");
		Map<Long, Long> map = messageService.findByGroupEntityAndDateAfter(groupEntity, new Date(time));
		StringBuilder sb = new StringBuilder().append("本群今日发言数统计如下：").append("\n");
		for (Map.Entry<Long, Long> entry: map.entrySet()){
			sb.append("@");
			try {
				sb.append(qq.getNameCard());
			}catch (Exception e){
				sb.append("未在本群");
			}
			sb.append("（").append(entry.getKey()).append("）").append("：")
					.append(entry.getValue()).append("条").append("\n");
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	@Action("窥屏检测")
	public void checkPeeping(Group group){
		String api = "https://api.kuku.me";
		String random = MyUtils.randomNum(4);
		String jsonStr = "{\"app\":\"com.tencent.miniapp\",\"desc\":\"\",\"view\":\"notification\",\"ver\":\"1.0.0.11\",\"prompt\":\"QQ程序\",\"appID\":\"\",\"sourceName\":\"\",\"actionData\":\"\",\"actionData_A\":\"\",\"sourceUrl\":\"\",\"meta\":{\"notification\":{\"appInfo\":{\"appName\":\"三楼有只猫\",\"appType\":4,\"appid\":1109659848,\"iconUrl\":\"" + api + "\\/tool\\/peeping\\/check\\/" + random + "\"},\"button\":[],\"data\":[],\"emphasis_keyword\":\"\",\"title\":\"请等待15s\"}},\"text\":\"\",\"extraApps\":[],\"sourceAd\":\"\",\"extra\":\"\"}";
		group.sendMessage(mif.jsonEx(jsonStr).toMessage());
		jobManager.registerTimer(() -> {
			String msg;
			try {
				JSONObject jsonObject = OkHttpUtils.getJson(api + "/tool/peeping/result/" + random);
				if (jsonObject.getInteger("code") == 200) {
					StringBuilder sb = new StringBuilder();
					JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
					sb.append("检测到共有").append(jsonArray.size()).append("位小伙伴在窥屏").append("\n");
					for (int i = 0; i < jsonArray.size(); i++) {
						JSONObject singleJsonObject = jsonArray.getJSONObject(i);
						sb.append(singleJsonObject.getString("ip"))
								.append("-").append(singleJsonObject.getString("address"))
								/*.append("-").append(singleJsonObject.getString("simpleUserAgent"))*/.append("\n");
					}
					msg = MyUtils.removeLastLine(sb);
				} else msg = jsonObject.getString("message");
			} catch (IOException e) {
				e.printStackTrace();
				msg = "查询失败，请重试！！";
			}
			group.sendMessage(Message.Companion.toMessage(msg));
		}, 15 * 1000);
	}

	@Action("妹子图")
	public Object girlImage(long qq){
		byte[] bytes = toolLogic.girlImageGaNk();
		if (bytes != null){
			return mif.imageByByteArray(bytes);
		}else return "图片获取失败，请重试！！";
	}

	@Action("妹子")
	public Image girls() throws IOException {
		return mif.imageByByteArray(OkHttpUtils.getBytes(toolLogic.girl()));
	}

	@Action("\\.*\\")
	@QMsg(mastAtBot = true, at = true)
	public String talk(Message message) throws IOException {
		String msg = Message.Companion.firstString(message);
		ConfigEntity configEntity = configService.findByType("tuLing");
		if (configEntity == null) return toolLogic.qinYunKeChat(msg);
		else {
			JSONObject contentJsonObject = configEntity.getContentJsonObject();
			Result<String> result = toolLogic.tuLing(contentJsonObject.getString("apiKey"),
					contentJsonObject.getString("userid"), msg);
			if (result.isSuccess()) return result.getData();
			else return result.getMessage();
		}
	}

	@Action("京东代挂")
	public void jd(long qq, Group group) throws IOException {
		String url = "https://api.kuku.me";
		JSONObject jsonObject = OkHttpUtils.postJson(url + "/jd/qrcode", new HashMap<>());
		if (jsonObject.getInteger("code") != 200) {
			group.sendMessage(mif.at(qq).plus(jsonObject.getString("message")));
			return;
		}
		JSONObject dataJsonObject = jsonObject.getJSONObject("data");
		dataJsonObject.put("type", "0");
		String qrcodeUrl = dataJsonObject.getString("qrcodeUrl");
		group.sendMessage(mif.at(qq).plus(mif.imageByInputStream(toolLogic.creatQr(qrcodeUrl)).plus("请使用京东app扫码登录！")));
		jobManager.registerTimer(() -> {
			String msg;
			while (true){
				try {
					TimeUnit.SECONDS.sleep(3);
					JSONObject cookieJsonObject = OkHttpUtils.postJson(url + "/jd/cookie", dataJsonObject.toJavaObject(Map.class));
					Integer code = cookieJsonObject.getInteger("code");
					if (code == 200){
						msg = "添加京东至青龙面板成功！";
						break;
					}else if (code == 505){
						msg = "二维码已失效！";
						break;
					}else if (code == 506){
						msg = "未配置配置文件信息！";
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg = "出现异常了，异常信息为：" + e.getMessage();
					break;
				}
			}
			group.sendMessage(mif.at(qq).plus(msg));
		}, 0);
	}

	@Action("读懂世界")
	@QMsg(at = true, atNewLine = true)
	public String readWorld() throws IOException {
		return OkHttpUtils.getJson("https://api.kuku.me/tool/readWorld").getJSONObject("data")
				.getString("data");
	}

	@Action("消息统计")
	public String message(){
		return "当前收发消息状态：\n" +
				"收：" + rainInfo.getCountRm() + " / 分钟\n" +
				"发：" + rainInfo.getCountSm() + " / 分钟\n" +
				"总计：\n" +
				"收：" + rainInfo.getCountRa() + " 条，\n" +
				"发：" + rainInfo.getCountSa() + " 条。";
	}

	@Action("code {type}")
	@QMsg(at = true, atNewLine = true)
	public String codeExecute(ContextSession session, Group group, long qq, String type) throws IOException {
		CodeType codeType = CodeType.parse(type);
		if (codeType == null) return "没有找到这个语言类型！！";
		group.sendMessage(mif.at(qq).plus("请输入代码！！"));
		Message message = session.waitNextMessage();
		String code = Message.Companion.firstString(message);
		return toolLogic.executeCode(code, codeType);
	}

	@Action("shell {command}")
	@QMsg(at = true)
	public String shellCommand(String command, Group group, long qq, @PathVar(2) String ss){
		GroupEntity groupEntity = groupService.findByGroup(group.getId());
		String errorMsg = "没有找到这个命令，请重试！！";
		if (groupEntity == null) return errorMsg;
		JSONArray jsonArray = groupEntity.getShellCommandJson();
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			if (jsonObject.getString("command").equals(command)){
				//0为主人，1为超管，2为普管，3为用户
				Integer auth = jsonObject.getInteger("auth");
				boolean b;
				if (auth == 0){
					b = qq == Long.parseLong(master);
				}else if (auth == 1){
					b = groupEntity.isAdmin(qq);
				}else b = true;
				if (b){
					String shell = jsonObject.getString("shell");
					jobManager.registerTimer(() -> {
						Runtime runtime = Runtime.getRuntime();
						try {
							String os = System.getProperty("os.name");
							Process process = runtime.exec(shell);
							if (ss == null) {
								byte[] bytes = IO.read(process.getInputStream(), true);
								String result;
								if (os.contains("Windows")) result = new String(bytes, "gbk");
								else result = new String(bytes, StandardCharsets.UTF_8);
								group.sendMessage(mif.at(qq).plus("脚本执行成功，信息如下：\n").plus(result));
							}else group.sendMessage(mif.at(qq).plus("脚本正在后台执行中！！"));
						} catch (IOException e) {
							e.printStackTrace();
							group.sendMessage(mif.at(qq).plus("脚本执行失败！！"));
						}
					}, 0);
					return "shell命令正在执行中，请稍后！！";
				}else return "您的权限不足，无法执行这个命令";
			}
		}
		return errorMsg;
	}
}
