package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.NetEaseEntity;
import me.kuku.yuq.entity.NetEaseService;
import me.kuku.yuq.logic.NetEaseLogic;

import javax.inject.Inject;
import java.util.List;

@JobCenter
public class NetEaseJob {

	@Inject
	private NetEaseService netEaseService;
	@Inject
	private NetEaseLogic netEaseLogic;

	@Cron("At::d::08:06:32")
	public void sign(){
		List<NetEaseEntity> list = netEaseService.findAll();
		for (NetEaseEntity netEaseEntity : list) {
			try {
				netEaseLogic.sign(netEaseEntity);
				netEaseLogic.listeningVolume(netEaseEntity);
				netEaseLogic.musicianSign(netEaseEntity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Cron("1h")
	public void musicianSign(){
		List<NetEaseEntity> list = netEaseService.findAll();
		for (NetEaseEntity netEaseEntity : list) {
			try {
				netEaseLogic.musicianSign(netEaseEntity);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

}
