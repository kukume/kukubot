package me.kuku.simbot.scheduled;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.*;
import me.kuku.simbot.logic.QqLoginLogic;
import me.kuku.simbot.logic.QqMusicLogic;
import me.kuku.simbot.logic.ToolLogic;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
	@Resource
	private ToolLogic toolLogic;

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
	@Transactional
	public void musicSign(){
		List<QqMusicEntity> list = qqMusicService.findAll();
		for (QqMusicEntity qqMusicEntity : list) {
			try {
				Result<Void> result = qqMusicLogic.sign(qqMusicEntity);
				if (result.isFailure()){
					QqEntity qqEntity = qqMusicEntity.getQqEntity();
					BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(), "您的QQ音乐的cookie已失效，如需自动签到，请重新绑定！");
					qqMusicService.delete(qqMusicEntity);
					continue;
				}
				qqMusicLogic.musicianSign(qqMusicEntity);
				for (int i = 0; i < 3; i++){
					TimeUnit.SECONDS.sleep(3);
					qqMusicLogic.randomReplyComment(qqMusicEntity, "太好听了把！");
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Scheduled(cron = "21 12 3 1 */1 ?")
	public void musicianTask(){
		List<QqMusicEntity> list = qqMusicService.findAll();
		for (QqMusicEntity qqMusicEntity : list) {
			try {
				for (int i = 0; i < 3; i++) {
					TimeUnit.SECONDS.sleep(3);
					Result<Void> result = qqMusicLogic.publishNews(qqMusicEntity, toolLogic.hiToKoTo().get("text"));
					if (result.isFailure()){
						QqEntity qqEntity = qqMusicEntity.getQqEntity();
						BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(), "您的QQ音乐发布新动态失败，请手动发布新动态完成任务~");
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


}
