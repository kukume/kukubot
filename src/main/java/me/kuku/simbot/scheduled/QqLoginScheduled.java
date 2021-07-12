package me.kuku.simbot.scheduled;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.simbot.logic.QqLoginLogic;
import me.kuku.simbot.service.QqLoginService;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Component
public class QqLoginScheduled {

	@Resource
	private QqLoginService qqLoginService;
	@Resource
	private QqLoginLogic qqLoginLogic;


	@Scheduled(cron = "32 3 6 * * ?")
	@Transactional
	public void qqSign(){
		List<QqLoginEntity> list = qqLoginService.findAll();
		for (QqLoginEntity qqLoginEntity : list) {
			try {
				String str = qqLoginLogic.vipSign(qqLoginEntity);
				if (str.contains("更新QQ")){
					QqEntity qqEntity = qqLoginEntity.getQqEntity();
					BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(), "您的QQ已失效，如需自动签到，请重新绑定！");
					continue;
				}
				qqLoginLogic.yellowSign(qqLoginEntity);
				qqLoginLogic.qqVideoSign1(qqLoginEntity);
				qqLoginLogic.qqVideoSign2(qqLoginEntity);
				qqLoginLogic.bigVipSign(qqLoginEntity);
				qqLoginLogic.qqMusicSign(qqLoginEntity);
				qqLoginLogic.gameSign(qqLoginEntity);
				qqLoginLogic.qPetSign(qqLoginEntity);
				qqLoginLogic.blueSign(qqLoginEntity);
				qqLoginLogic.weiYunSign(qqLoginEntity);
				qqLoginLogic.growthLike(qqLoginEntity);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}
