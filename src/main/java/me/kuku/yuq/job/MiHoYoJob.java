package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.logic.MiHoYoLogic;
import me.kuku.yuq.service.MiHoYoService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@JobCenter
public class MiHoYoJob {

	@Inject
	private MiHoYoLogic miHoYoLogic;
	@Inject
	private MiHoYoService miHoYoService;

	@Cron("At::d::08:30")
	public void genShinSign(){
		List<MiHoYoEntity> list = miHoYoService.findAll();
		list.forEach(entity -> {
			try {
				miHoYoLogic.sign(entity);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
