package me.kuku.yuq.controller.mihoyo;

import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.logic.MiHoYoLogic;
import me.kuku.yuq.service.MiHoYoService;

import javax.inject.Inject;

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
		else throw FunKt.getMif().at(qq).plus("xxxx").toThrowable();
	}
}
