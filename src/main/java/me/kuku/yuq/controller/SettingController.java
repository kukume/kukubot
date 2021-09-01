package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.pojo.Result;
import me.kuku.utils.MyUtils;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.BaiduAiPojo;
import me.kuku.yuq.logic.OfficeGlobalLogic;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("DuplicatedCode")
@PrivateController
public class SettingController extends QQController {

	@Inject
	private GroupService groupService;
	@Inject
	private OfficeGlobalService officeGlobalService;
	@Inject
	private OfficeGlobalLogic officeGlobalLogic;
	@Config("YuQ.Mirai.bot.master")
	private String master;
	@Inject
	private ConfigService configService;

	@Before
	public void before(long qq){
		if (!Objects.equals(master, String.valueOf(qq)))
			throw FunKt.getMif().at(qq).plus("您不是机器人主人，无法执行！").toThrowable();
	}

	@Action("群开启 {groupNo}")
	@Synonym("群关闭 {groupNo}")
	public String groupOpenOrClose(@PathVar(0) String op, Long groupNo){
		GroupEntity groupEntity = groupService.findByGroup(groupNo);
		if (groupEntity == null) return "机器人可能没有加入这个群，如果确定加入了，请在该群随便发送一条消息";
		groupEntity.setStatus(op.equals("群开启"));
		groupService.save(groupEntity);
		return op + "成功！";
	}

	@Action("退群 {groupNo}")
	public String leaveGroup(long groupNo, Group group){
		try {
			group.leave();
			return "退出群聊成功！";
		}catch (Exception e){
			return "退出群聊失败，异常信息：" + e.getMessage();
		}
	}

	@Action("绑全局")
	public String bindOfficeGlobal(ContextSession session){
		reply("请输入该全局显示的名称");
		String name = Message.Companion.firstString(session.waitNextMessage());
		OfficeGlobalEntity officeGlobalEntity = officeGlobalService.findByName(name);
		if (officeGlobalEntity != null){
			return "绑定全局失败，该名称已存在！";
		}
		officeGlobalEntity = new OfficeGlobalEntity();
		officeGlobalEntity.setName(name);
		reply("请输入clientId");
		String clientId = Message.Companion.firstString(session.waitNextMessage());
		reply("请输入clientSecret");
		String clientSecret = Message.Companion.firstString(session.waitNextMessage());
		reply("请输入tenantId");
		String tenantId = Message.Companion.firstString(session.waitNextMessage());
		reply("请输入domain");
		String domain = Message.Companion.firstString(session.waitNextMessage());
		reply("请输入订阅显示名称和订阅ID，名称和ID以|分割，如果有多个订阅，请使用;分割");
		String ss = Message.Companion.firstString(session.waitNextMessage(1000 * 60 * 5));
		String[] arr = ss.split(";");
		List<Sku> list = new ArrayList<>();
		for (String sss : arr) {
			Sku sku = new Sku();
			String[] strArr = sss.split("\\|");
			sku.setName(strArr[0]);
			sku.setId(strArr[1]);
			list.add(sku);
		}
		officeGlobalEntity.setClientId(clientId);
		officeGlobalEntity.setClientSecret(clientSecret);
		officeGlobalEntity.setTenantId(tenantId);
		officeGlobalEntity.setDomain(domain);
		officeGlobalEntity.setSKuJson(list);
		officeGlobalService.save(officeGlobalEntity);
		return "绑定全局信息成功！";
	}

	@Action("删全局 {name}")
	public String deleteOffice(String name){
		officeGlobalService.deleteByName(name);
		return "删除全局成功！";
	}

	@Action("查全局")
	public String queryOffice(){
		List<OfficeGlobalEntity> list = officeGlobalService.findAll();
		StringBuilder sb = new StringBuilder().append("您绑定的office全局如下：").append("\n");
		list.forEach(it -> sb.append(it.getName()).append("\n"));
		return MyUtils.removeLastLine(sb);
	}

	@Action("office提权 {mail}")
	public String setAdmin(String mail, ContextSession session) throws IOException {
		List<OfficeGlobalEntity> officeList = officeGlobalService.findAll();
		if (officeList.size() == 0) return "管理员还没有绑定office信息，创建失败！";
		int officeIndex = 0;
		if (officeList.size() > 1){
			StringBuilder sb = new StringBuilder("请选择您需要创建的全局名称，回复序号数字即可").append("\n");
			for (int i = 0; i < officeList.size(); i++){
				sb.append(i).append("、").append(officeList.get(i).getName()).append("\n");
			}
			reply(MyUtils.removeLastLine(sb));
			String numStr = Message.Companion.firstString(session.waitNextMessage());
			if (!numStr.matches("[0-9]+")) return "回复的不为数字！";
			int num = Integer.parseInt(numStr);
			if (num > officeList.size() - 1) return "回复的数字超限！";
			officeIndex = num;
		}
		OfficeGlobalEntity officeGlobalEntity = officeList.get(officeIndex);
		Result<?> result = officeGlobalLogic.userToAdmin(officeGlobalEntity, mail, OfficeRole.GLOBAL_ADMIN);
		return result.getMessage();
	}

	@Action("加问答 {q}")
	public String qa(ContextSession session, String q, @PathVar(value = 2, type = PathVar.Type.Long) Long group){
		List<GroupEntity> list = new ArrayList<>();
		if (group == null) list = groupService.findAll();
		else {
			GroupEntity groupEntity = groupService.findByGroup(group);
			if (groupEntity == null) return "没有找到这个群号，请检查后重试！";
			list.add(groupEntity);
		}
		reply("请输入问答类型，1为精准匹配，其他为模糊匹配");
		String type;
		String typeMsg = Message.Companion.firstString(session.waitNextMessage());
		if ("1".equals(typeMsg)) type = "ALL";
		else type = "PARTIAL";
		reply("请输入回答语句！！");
		String msg = Message.Companion.toCodeString(session.waitNextMessage());
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("q", q);
		jsonObject.put("a", msg);
		jsonObject.put("type", type);
		for (GroupEntity groupEntity : list) {
			JSONArray jsonArray = groupEntity.getQaJson();
			jsonArray.add(jsonObject);
			groupEntity.setQaJson(jsonArray);
			groupService.save(groupEntity);
		}
		return "添加问答成功！！";
	}

	@Action("删问答 {q} {group}")
	public String deleteQa(String group, String q){
		List<GroupEntity> list = new ArrayList<>();
		if ("ALL".equals(group)) list = groupService.findAll();
		else {
			GroupEntity groupEntity = groupService.findByGroup(Long.parseLong(group));
			if (groupEntity == null) return "没有找到这个群号，请检查后重试！";
			list.add(groupEntity);
		}
		for (GroupEntity groupEntity : list) {
			List<JSONObject> delList = new ArrayList<>();
			JSONArray jsonArray = groupEntity.getQaJson();
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (q.equals(jsonObject.getString("q"))){
					delList.add(jsonObject);
				}
			}
			delList.forEach(jsonArray::remove);
			groupEntity.setQaJson(jsonArray);
			groupService.save(groupEntity);
		}
		return "删除问答成功！";
	}

	@Action("查问答 {group}")
	public String queryQa(@PathVar(value = 2, type = PathVar.Type.Long) Long group){
		GroupEntity groupEntity = groupService.findByGroup(group);
		if (groupEntity == null) return "没有找到这个群号，请检查后重试！";
		StringBuilder sb = new StringBuilder();
		sb.append("本群问答列表如下：").append("\n");
		groupEntity.getQaJson().forEach(obj -> {
			JSONObject jsonObject = (JSONObject) obj;
			sb.append(jsonObject.getString("q")).append("-").append(jsonObject.getString("type")).append("\n");
		});
		return sb.toString();
	}

	@Action("图灵 {apiKey} {userid}")
	public String bindTuLing(@PathVar(1) String apiKey, @PathVar(2) String userid){
		String type = "tuLing";
		if (apiKey == null || userid == null){
			configService.deleteByType(type);
			return "删除图灵信息成功！";
		}else {
			ConfigEntity configEntity = configService.findByType(type);
			if (configEntity == null) configEntity = ConfigEntity.Companion.getInstance(type);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("apiKey", apiKey);
			jsonObject.put("userid", userid);
			configEntity.setContentJsonObject(jsonObject);
			configService.save(configEntity);
			return "绑定图灵信息成功！";
		}
	}

	@Action("百度ocr")
	@Synonym({"百度内容审核"})
	public String settingBaiduAI(ContextSession session, @PathVar(0) String type){
		reply("请输入apiKey");
		Message apiKeyMessage = session.waitNextMessage();
		String apiKey = Message.Companion.firstString(apiKeyMessage);
		reply("请输入secretKey");
		Message secretKeyMessage = session.waitNextMessage();
		String secretKey = Message.Companion.firstString(secretKeyMessage);
		ConfigEntity configEntity = configService.findByType("baiduAi");
		if (configEntity == null) configEntity = ConfigEntity.Companion.getInstance("baiduAi");
		BaiduAiPojo baiduAiPojo = configEntity.getConfigParse(BaiduAiPojo.class);
		switch (type){
			case "百度ocr":
				baiduAiPojo.setOcrApiKey(apiKey);
				baiduAiPojo.setOcrSecretKey(secretKey);
				break;
			case "百度内容审核":
				baiduAiPojo.setAntiPornApiKey(apiKey);
				baiduAiPojo.setAntiPornSecretKey(secretKey);
				break;
		}
		configEntity.setConfigParse(baiduAiPojo);
		configService.save(configEntity);
		return "绑定百度AI的信息成功！！";
	}
}
