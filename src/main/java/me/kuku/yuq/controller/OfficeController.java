package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.alibaba.fastjson.JSON;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.entity.OfficeCodeEntity;
import me.kuku.yuq.logic.OfficeUserLogic;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.OfficePojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.OfficeCodeService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
@PrivateController
public class OfficeController {

	@Inject
	private OfficeCodeService officeCodeService;
	@Inject
	private OfficeUserLogic officeUserLogic;
	@Inject
	private ConfigService configService;

	@Action("office创建用户")
	public synchronized String createUser(Contact qq, ContextSession session) throws IOException {
		ConfigEntity configEntity = configService.findByType(ConfigType.OFFICE_USER.getType());
		if (configEntity == null) return "管理员还没有绑定office信息，创建失败！";
		qq.sendMessage(BotUtils.toMessage("请输入激活码！"));
		Message codeMessage = session.waitNextMessage();
		String code = BotUtils.firstString(codeMessage);
		OfficeCodeEntity entity = officeCodeService.findByCode(code);
		if (entity == null || entity.getIsUse()) return "该code不存在或已使用！";
		String str = configEntity.getContent();
		List<OfficePojo> officeList = JSON.parseArray(str, OfficePojo.class);
		if (officeList.size() == 0) return "管理员还没有绑定office信息，创建失败！";
		int officeIndex = 0;
		if (officeList.size() > 1){
			StringBuilder sb = new StringBuilder("请选择您需要创建的全局名称，回复序号数字即可").append("\n");
			for (int i = 0; i < officeList.size(); i++){
				sb.append(i).append("、").append(officeList.get(i).getName()).append("\n");
			}
			qq.sendMessage(BotUtils.toMessage(BotUtils.removeLastLine(sb)));
			Message ssMessage = session.waitNextMessage();
			String numStr = BotUtils.firstString(ssMessage);
			if (!numStr.matches("[0-9]+")) return "回复的不为数字！";
			int num = Integer.parseInt(numStr);
			if (num > officeList.size() - 1) return "回复的数字超限！";
			officeIndex = num;
		}
		OfficePojo officePojo = officeList.get(officeIndex);
		qq.sendMessage(BotUtils.toMessage("请输入你需要创建的用户名"));
		Message nextMessage = session.waitNextMessage();
		String username = BotUtils.firstString(nextMessage);
		List<OfficePojo.Sku> skuList = officePojo.getSku();
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
		Result<?> result = officeUserLogic.createUser(officePojo, username, username, password, index);
		if (result.isSuccess()) {
			entity.setIsUse(true);
			officeCodeService.save(entity);
			return "创建成功，您的用户信息如下：\n" +
					"订阅：" + skuList.get(index).getName() + "\n" +
					"邮箱：" + username + "@" + officePojo.getDomain() + "\n" +
					"密码：" + password + "\n" +
					"登录地址：https://portal.office.com";
		}
		else return "创建失败！" + result.getMessage();
	}

	@PrivateController
	public static class OfficeAdminController{
		@Inject
		private OfficeCodeService officeCodeService;
		@Config("YuQ.Mirai.bot.master")
		private String master;

		@Before
		public void before(long qq) {
			if (!Long.valueOf(master).equals(qq)) {
				throw FunKt.getMif().text("您不是主人，无法执行！").toMessage().toThrowable();
			}
		}
		@Action("office生成码子")
		public String createCode(long qq, @PathVar(1) String numStr){
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
			list.stream().map(OfficeCodeEntity::new).forEach(officeCodeService::save);
			StringBuilder sb = new StringBuilder().append("您本次生成的code如下：").append("\n");
			list.forEach(it -> sb.append(it).append("\n"));
			return BotUtils.removeLastLine(sb);
		}

		@Action("查office码子")
		public String queryCode(){
			List<OfficeCodeEntity> list = officeCodeService.findAll();
			StringBuilder sb = new StringBuilder().append("office激活码如下：").append("\n");
			list.forEach(it -> {
				sb.append(it.getCode()).append("->").append(it.getIsUse() ? "已使用" : "未使用").append("\n");
			});
			return BotUtils.removeLastLine(sb);
		}

		@Action("删office码子")
		public String delCode(){
			List<OfficeCodeEntity> list = officeCodeService.findAll();
			List<String> codeList = list.stream().filter(OfficeCodeEntity::getIsUse)
					.map(OfficeCodeEntity::getCode).collect(Collectors.toList());
			codeList.forEach(it -> officeCodeService.delByCode(it));
			return "删除已使用的码子成功！";
		}
	}

}
