package me.kuku.simbot.controller;

import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.GroupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OnPrivate
public class SettingController {

	@Resource
	private GroupService groupService;

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


}
