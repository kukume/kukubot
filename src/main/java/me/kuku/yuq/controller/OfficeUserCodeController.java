package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.alibaba.fastjson.JSON;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.entity.OfficeUserCodeEntity;
import me.kuku.yuq.logic.OfficeUserLogic;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.OfficeUserPojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.OfficeUserCodeService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@PrivateController
public class OfficeUserCodeController {

	@Inject
	private OfficeUserCodeService officeUserCodeService;
	@Inject
	private OfficeUserLogic officeUserLogic;
	@Config("YuQ.Mirai.bot.master")
	private String master;
	@Inject
	private ConfigService configService;

	@Action("officeuser生成code")
	public String createCode(long qq, @PathVar(1) String numStr){
		if (Long.valueOf(master).equals(qq)){
			int num;
			if (numStr == null) num = 1;
			else {
				if (!numStr.matches("[0-9]+")) return "需要生成的code个数不为整数";
				else num = Integer.parseInt(numStr);
			}
			List<String> list = new ArrayList<>();
			for (int i = 0; i < num; i++){
				list.add(BotUtils.randomStr(8));
			}
			list.stream().map(OfficeUserCodeEntity::new).forEach(officeUserCodeService::save);
			StringBuilder sb = new StringBuilder().append("您本次生成的code如下：").append("\n");
			list.forEach(it -> sb.append(it).append("\n"));
			return BotUtils.removeLastLine(sb);
		}else return "您不是主人，无法执行";
	}

	@Action("officeuser创建 {code}")
	public synchronized String createUser(String code, Contact qq, ContextSession session) throws IOException {
		OfficeUserCodeEntity entity = officeUserCodeService.findByCode(code);
		if (entity == null || entity.getIsUse()) return "该code不存在或已使用！";
		ConfigEntity configEntity = configService.findByType(ConfigType.OFFICE_USER.getType());
		if (configEntity == null) return "管理员还没有绑定office信息，创建失败！";
		String str = configEntity.getContent();
		OfficeUserPojo officeUserPojo = JSON.parseObject(str, OfficeUserPojo.class);
		qq.sendMessage(BotUtils.toMessage("请输入你需要创建的用户名"));
		Message nextMessage = session.waitNextMessage();
		String username = BotUtils.firstString(nextMessage);
		List<OfficeUserPojo.Sku> skuList = officeUserPojo.getSku();
		int index = 0;
		if (skuList.size() > 1){
			StringBuilder sb = new StringBuilder().append("请选择您需要创建的订阅，回复序号数字即可").append("\n");
			for (int i = 0; i < skuList.size(); i++){
				sb.append(i).append("、").append(skuList.get(i).getName()).append("\n");
			}
			qq.sendMessage(BotUtils.toMessage(BotUtils.removeLastLine(sb)));
			Message ssMessage = session.waitNextMessage();
			String numStr = BotUtils.firstString(ssMessage);
			if (!numStr.matches("[0-9]+")) return "回复的不为数字！";
			int num = Integer.parseInt(numStr);
			if (num > skuList.size() - 1) return "回复的数字超限！";
			index = num;
		}
		String password = BotUtils.randomStr(8);
		Result<?> result = officeUserLogic.createUser(officeUserPojo, username, username, password, index);
		if (result.isSuccess()) {
			entity.setIsUse(true);
			officeUserCodeService.save(entity);
			return "创建成功，您的用户信息如下：\n" +
					"订阅：" + skuList.get(index).getName() + "\n" +
					"邮箱：" + username + "@" + officeUserPojo.getDomain() + "\n" +
					"密码：" + password + "\n" +
					"登录地址：https://portal.office.com";
		}
		else return "创建失败！" + result.getMessage();
	}


}
