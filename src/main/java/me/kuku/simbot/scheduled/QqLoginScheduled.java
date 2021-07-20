package me.kuku.simbot.scheduled;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.simbot.entity.QqMusicEntity;
import me.kuku.simbot.entity.QqVideoEntity;
import me.kuku.simbot.logic.QqLoginLogic;
import me.kuku.simbot.logic.QqMusicLogic;
import me.kuku.simbot.service.QqLoginService;
import me.kuku.simbot.service.QqMusicService;
import me.kuku.simbot.service.QqVideoService;
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
	@Resource
	private QqVideoService qqVideoService;
	@Resource
	private QqMusicService qqMusicService;
	@Resource
	private QqMusicLogic qqMusicLogic;

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
					qqLoginService.delete(qqLoginEntity);
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

	@Scheduled(cron = "42 23 */1 * * ?")
	public void videoSign(){
		List<QqVideoEntity> list = qqVideoService.findAll();
		for (QqVideoEntity qqVideoEntity : list) {
			try {
				qqLoginLogic.videoSign(qqVideoEntity);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Scheduled(cron = "41 15 5 * * ?")
	public void musicSign(){
		List<QqMusicEntity> list = qqMusicService.findAll();
		for (QqMusicEntity qqMusicEntity : list) {
			try {
				Result<Void> result = qqMusicLogic.sign(qqMusicEntity);
				if (result.isFailure()){
					QqEntity qqEntity = qqMusicEntity.getQqEntity();
					BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(), "您的QQ已失效，如需自动签到，请重新绑定！");
					qqMusicService.delete(qqMusicEntity);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}


}
