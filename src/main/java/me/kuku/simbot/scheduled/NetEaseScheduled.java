package me.kuku.simbot.scheduled;

import me.kuku.simbot.entity.NetEaseEntity;
import me.kuku.simbot.entity.NetEaseService;
import me.kuku.simbot.logic.NetEaseLogic;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class NetEaseScheduled {

	@Resource
	private NetEaseService netEaseService;
	@Resource
	private NetEaseLogic netEaseLogic;

	@Scheduled(cron = "32 6 8 * * ?")
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

}
