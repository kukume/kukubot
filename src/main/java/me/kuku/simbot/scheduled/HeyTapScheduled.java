package me.kuku.simbot.scheduled;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.HeyTapEntity;
import me.kuku.simbot.entity.HeyTapService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.HeyTapLogic;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class HeyTapScheduled {

	@Resource
	private HeyTapLogic heyTapLogic;
	@Resource
	private HeyTapService heyTapService;

	@Scheduled(cron = "12 51 6 * * ?")
	@Transactional
	public void heyTapSign(){
		List<HeyTapEntity> list = heyTapService.findAll();
		for (HeyTapEntity heyTapEntity : list) {
			try {
				Result<Void> result = heyTapLogic.sign(heyTapEntity);
				if (result.isFailure()){
					QqEntity qqEntity = heyTapEntity.getQqEntity();
					BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(),
							"您的欢太账号cookie已失效，请重新绑定！");
					heyTapService.delete(heyTapEntity);
					continue;
				}
				heyTapLogic.viewGoods(heyTapEntity);
				heyTapLogic.shareGoods(heyTapEntity);
				heyTapLogic.viewPush(heyTapEntity);
				heyTapLogic.transferPoints(heyTapEntity);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Scheduled(cron = "54 1 0 * * ?")
	@Transactional
	public void heyTapEarlyRe(){
		List<HeyTapEntity> list = heyTapService.findByEarlyToBedClock(true);
		for (HeyTapEntity heyTapEntity : list) {
			try {
				heyTapLogic.earlyBedRegistration(heyTapEntity);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Scheduled(cron = "10 30 19 * * ?")
	@Transactional
	public void heyTapEarlySign(){
		List<HeyTapEntity> list = heyTapService.findByEarlyToBedClock(true);
		for (HeyTapEntity heyTapEntity : list) {
			try {
				Result<Void> result = heyTapLogic.earlyBedRegistration(heyTapEntity);
				if (result.isFailure()){
					QqEntity qqEntity = heyTapEntity.getQqEntity();
					BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(),
							"您的欢太商城早睡打卡失败，请去手动打卡，24点之前未打卡将损失500积分！");
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}


}
