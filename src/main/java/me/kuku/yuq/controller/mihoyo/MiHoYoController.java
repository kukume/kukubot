package me.kuku.yuq.controller.mihoyo;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.logic.MiHoYoLogic;
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
}
