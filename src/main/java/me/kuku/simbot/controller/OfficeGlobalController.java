package me.kuku.simbot.controller;

import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.entity.OfficeGlobalEntity;
import me.kuku.simbot.entity.OfficeGlobalService;
import me.kuku.simbot.entity.Sku;
import me.kuku.simbot.logic.OfficeGlobalLogic;
import me.kuku.utils.MyUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
@Component
@OnPrivate
public class OfficeGlobalController {

	@Resource
	private OfficeGlobalService officeGlobalService;
	@Resource
	private OfficeGlobalLogic officeGlobalLogic;

	@Filter("office创建用户")
	public String createUser(ContextSession session, long qq, MsgSender msgSender) throws IOException {
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
		msgSender.SENDER.sendPrivateMsg(qq, "请输入你需要创建的用户名");
		String username = session.waitNextMessage();
		List<Sku> skuList = officeGlobalEntity.getSKuJson();
		int index = 0;
		if (skuList.size() > 1){
			StringBuilder sb = new StringBuilder().append("请选择您需要创建的订阅，回复序号数字即可").append("\n");
			for (int i = 0; i < skuList.size(); i++){
				sb.append(i).append("、").append(skuList.get(i).getName()).append("\n");
			}
			msgSender.SENDER.sendPrivateMsg(qq, MyUtils.removeLastLine(sb));
			String numStr = session.waitNextMessage();
			if (!numStr.matches("[0-9]+")) return "回复的不为数字！";
			int num = Integer.parseInt(numStr);
			if (num > skuList.size() - 1) return "回复的数字超限！";
			index = num;
		}
		String password = MyUtils.randomStr(8);
		Result<?> result = officeGlobalLogic.createUser(officeGlobalEntity, username, username, password, index);
		if (result.isSuccess()) {
			return "创建成功，您的用户信息如下：\n" +
					"订阅：" + skuList.get(index).getName() + "\n" +
					"邮箱：" + username + "@" + officeGlobalEntity.getDomain() + "\n" +
					"密码：" + password + "\n" +
					"登录地址：https://portal.office.com";
		}
		else return "创建失败！" + result.getMessage();
	}

}
