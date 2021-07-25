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


}
