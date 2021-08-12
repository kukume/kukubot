package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.pojo.Result;
import me.kuku.utils.MyUtils;
import me.kuku.yuq.entity.OfficeGlobalEntity;
import me.kuku.yuq.entity.OfficeGlobalService;
import me.kuku.yuq.entity.Sku;
import me.kuku.yuq.logic.OfficeGlobalLogic;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@PrivateController
public class OfficeGlobalController extends QQController {

	@Inject
	private OfficeGlobalService officeGlobalService;
	@Inject
	private OfficeGlobalLogic officeGlobalLogic;

	@Action("office创建用户")
	public String createUser(ContextSession session) throws IOException {
		List<OfficeGlobalEntity> officeList = officeGlobalService.findAll();
		if (officeList.size() == 0) return "管理员还没有绑定office信息，创建失败！";
		int officeIndex = 0;
		if (officeList.size() > 1){
			StringBuilder sb = new StringBuilder("请选择您需要创建的全局名称，回复序号数字即可").append("\n");
			for (int i = 0; i < officeList.size(); i++){
				sb.append(i).append("、").append(officeList.get(i).getName()).append("\n");
			}
			reply(MyUtils.removeLastLine(sb));
			Message numStrMessage = session.waitNextMessage();
			String numStr = Message.Companion.firstString(numStrMessage);
			if (!numStr.matches("[0-9]+")) return "回复的不为数字！";
			int num = Integer.parseInt(numStr);
			if (num > officeList.size() - 1) return "回复的数字超限！";
			officeIndex = num;
		}
		OfficeGlobalEntity officeGlobalEntity = officeList.get(officeIndex);
		reply("请输入你需要创建的用户名");
		String username = Message.Companion.firstString(session.waitNextMessage());
		List<Sku> skuList = officeGlobalEntity.getSKuJson();
		int index = 0;
		if (skuList.size() > 1){
			StringBuilder sb = new StringBuilder().append("请选择您需要创建的订阅，回复序号数字即可").append("\n");
			for (int i = 0; i < skuList.size(); i++){
				sb.append(i).append("、").append(skuList.get(i).getName()).append("\n");
			}
			reply(MyUtils.removeLastLine(sb));
			String numStr = Message.Companion.firstString(session.waitNextMessage());
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
