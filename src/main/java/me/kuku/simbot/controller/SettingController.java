package me.kuku.simbot.controller;

import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.*;
import me.kuku.simbot.logic.OfficeGlobalLogic;
import me.kuku.utils.MyUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
@Service
@OnPrivate
public class SettingController {

	@Resource
	private GroupService groupService;
	@Resource
	private OfficeGlobalService officeGlobalService;
	@Resource
	private OfficeGlobalLogic officeGlobalLogic;

	@RegexFilter("群{{op,开启|关闭}}{{groupNo}}")
	public String groupOpenOrClose(String op, Long groupNo){
		GroupEntity groupEntity = groupService.findByGroup(groupNo);
		if (groupEntity == null) return "机器人可能没有加入这个群，如果确定加入了，请在该群随便发送一条消息";
		groupEntity.setStatus(op.equals("开启"));
		groupService.save(groupEntity);
		return "机器人" + op + "成功！";
	}

	@RegexFilter("退群{{groupNo}}")
	public String leaveGroup(long groupNo, MsgSender msgSender){
		try {
			msgSender.SETTER.setGroupQuit(groupNo, false);
			return "退出群聊成功！";
		}catch (Exception e){
			return "退出群聊失败，异常信息：" + e.getMessage();
		}
	}

	@Filter("绑全局")
	public String bindOfficeGlobal(ContextSession session, MsgSender msgSender, long qq){
		msgSender.SENDER.sendPrivateMsg(qq,"请输入该全局显示的名称");
		String name = session.waitNextMessage();
		OfficeGlobalEntity officeGlobalEntity = officeGlobalService.findByName(name);
		if (officeGlobalEntity != null){
			return "绑定全局失败，该名称已存在！";
		}
		officeGlobalEntity = new OfficeGlobalEntity();
		officeGlobalEntity.setName(name);
		msgSender.SENDER.sendPrivateMsg(qq,"请输入clientId");
		String clientId = session.waitNextMessage();
		msgSender.SENDER.sendPrivateMsg(qq,"请输入clientSecret");
		String clientSecret = session.waitNextMessage();
		msgSender.SENDER.sendPrivateMsg(qq,"请输入tenantId");
		String tenantId = session.waitNextMessage();
		msgSender.SENDER.sendPrivateMsg(qq,"请输入domain");
		String domain = session.waitNextMessage();
		msgSender.SENDER.sendPrivateMsg(qq,"请输入订阅显示名称和订阅ID，名称和ID以|分割，如果有多个订阅，请使用;分割");
		String ss = session.waitNextMessage(1000 * 60 * 5);
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

	@RegexFilter("office提权{{mail}}")
	public String setAdmin(String mail, MsgSender msgSender, long qq, ContextSession session) throws IOException {
		List<OfficeGlobalEntity> officeList = officeGlobalService.findAll();
		if (officeList.size() == 0) return "管理员还没有绑定office信息，创建失败！";
		int officeIndex = 0;
		if (officeList.size() > 1){
			StringBuilder sb = new StringBuilder("请选择您需要创建的全局名称，回复序号数字即可").append("\n");
			for (int i = 0; i < officeList.size(); i++){
				sb.append(i).append("、").append(officeList.get(i).getName()).append("\n");
			}
			msgSender.SENDER.sendPrivateMsg(qq, MyUtils.removeLastLine(sb));
			String numStr = session.waitNextMessage();
			if (!numStr.matches("[0-9]+")) return "回复的不为数字！";
			int num = Integer.parseInt(numStr);
			if (num > officeList.size() - 1) return "回复的数字超限！";
			officeIndex = num;
		}
		OfficeGlobalEntity officeGlobalEntity = officeList.get(officeIndex);
		Result<?> result = officeGlobalLogic.userToAdmin(officeGlobalEntity, mail, OfficeRole.GLOBAL_ADMIN);
		return result.getMessage();
	}


}
