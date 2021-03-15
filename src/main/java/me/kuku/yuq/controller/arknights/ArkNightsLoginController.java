package me.kuku.yuq.controller.arknights;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.annotation.PrivateController;
import me.kuku.yuq.entity.ArkNightsEntity;
import me.kuku.yuq.logic.ArkNightsLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ArkNightsService;

import javax.inject.Inject;
import java.io.IOException;

@PrivateController
public class ArkNightsLoginController {
	@Inject
	private ArkNightsLogic arkNightsLogic;
	@Inject
	private ArkNightsService arkNightsService;

	@Action("ark {account} {password}")
	public String bindArk(String account, String password, long qq) throws IOException {
		Result<ArkNightsEntity> result = arkNightsLogic.login(account, password);
		if (result.isFailure()) return result.getMessage();
		ArkNightsEntity newArkNightsEntity = result.getData();
		ArkNightsEntity arkNightsEntity = arkNightsService.findByQQ(qq);
		if (arkNightsEntity == null) arkNightsEntity = new ArkNightsEntity(qq);
		arkNightsEntity.setCookie(newArkNightsEntity.getCookie());
		arkNightsService.save(arkNightsEntity);
		return "绑定ark成功！！";
	}
}
