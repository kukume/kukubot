package me.kuku.yuq.controller.mihoyo;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PrivateController;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.logic.MiHoYoLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.MiHoYoService;

import javax.inject.Inject;
import java.io.IOException;

@PrivateController
@GroupController
public class MiHoYoNotController {
	@Inject
	private MiHoYoLogic miHoYoLogic;
	@Inject
	private MiHoYoService miHoYoService;

	@Action("mihoyo {account} {password}")
	@Synonym({"米忽悠 {account} {password}", "米哈游 {account} {password}"})
	public String bindMiHoYo(String account, String password, long qq) throws IOException {
		Result<MiHoYoEntity> result = miHoYoLogic.login(account, password);
		if (result.isFailure()) return "绑定失败，" + result.getMessage();
		MiHoYoEntity newMiHoYoEntity = result.getData();
		MiHoYoEntity miHoYoEntity = miHoYoService.findByQQ(qq);
		if (miHoYoEntity == null) miHoYoEntity = new MiHoYoEntity(qq);
		miHoYoEntity.setAccount(account);
		miHoYoEntity.setPassword(password);
		miHoYoEntity.setCookie(newMiHoYoEntity.getCookie());
		miHoYoEntity.setAccountId(newMiHoYoEntity.getAccountId());
		miHoYoEntity.setTicket(newMiHoYoEntity.getTicket());
		miHoYoEntity.setCookieToken(newMiHoYoEntity.getCookieToken());
		miHoYoService.save(miHoYoEntity);
		return "绑定米忽悠成功！！";
	}


	@Action("genshin {id}")
	public String queryGenShinUserInfo(long id) throws IOException {
		return miHoYoLogic.genShinUserInfo(id);
	}
}
