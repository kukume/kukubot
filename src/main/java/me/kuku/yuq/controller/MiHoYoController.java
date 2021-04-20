package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.annotation.QMsg;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.logic.MiHoYoLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.MiHoYoService;

import javax.inject.Inject;
import java.io.IOException;

@GroupController
public class MiHoYoController {
	@Inject
	private MiHoYoService miHoYoService;
	@Inject
	private MiHoYoLogic miHoYoLogic;

	@Before
	public MiHoYoEntity before(long qq){
		MiHoYoEntity miHoYoEntity = miHoYoService.findByQQ(qq);
		if (miHoYoEntity != null) return miHoYoEntity;
		else throw FunKt.getMif().at(qq).plus("您还没有绑定米哈游账号，请私聊机器人<mihoyo 账号 密码>进行绑定").toThrowable();
	}

	@Action("genshin")
	public String genShinUserInfo(MiHoYoEntity miHoYoEntity) throws IOException {
		return miHoYoLogic.genShinUserInfo(miHoYoEntity, null);
	}

	@PrivateController
	@GroupController
	public static class MiHoYoLoginController {
		@Inject
		private MiHoYoLogic miHoYoLogic;
		@Inject
		private MiHoYoService miHoYoService;
		@Config("YuQ.Mirai.bot.master")
		private String master;

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
		@QMsg(at = true, atNewLine = true)
		public String queryGenShinUserInfo(long id) throws IOException {
			MiHoYoEntity miHoYoEntity = miHoYoService.findByQQ(Long.parseLong(master));
			if (miHoYoEntity == null) return "无法查询，请联系机器人主人绑定原神账号（查询信息需要cookie）";
			return miHoYoLogic.genShinUserInfo(miHoYoEntity, id);
		}
	}
}
