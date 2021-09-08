package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.QqLoginLogic;
import me.kuku.yuq.logic.QqMusicLogic;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@JobCenter
public class QqLoginJob {

	@Inject
	private QqLoginService qqLoginService;
	@Inject
	private QqLoginLogic qqLoginLogic;
	@Inject
	private QqVideoService qqVideoService;
	@Inject
	private QqMusicService qqMusicService;
	@Inject
	private QqMusicLogic qqMusicLogic;
	@Inject
	private ToolLogic toolLogic;

	@Cron("At::d::06:03:32")
	@Transactional
	public void qqSign(){
		List<QqLoginEntity> list = qqLoginService.findAll();
		for (QqLoginEntity qqLoginEntity : list) {
			try {
				String str = qqLoginLogic.vipSign(qqLoginEntity);
				if (str.contains("更新QQ")){
					QqEntity qqEntity = qqLoginEntity.getQqEntity();
					BotUtils.sendMessage(qqEntity, "您的QQ已失效，如需自动签到，请重新绑定！");
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

	@Cron("1h")
	@Transactional
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



}
