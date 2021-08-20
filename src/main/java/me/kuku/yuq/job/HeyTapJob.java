package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.HeyTapEntity;
import me.kuku.yuq.entity.HeyTapService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.entity.QqMusicEntity;
import me.kuku.yuq.logic.HeyTapLogic;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.util.List;

@JobCenter
public class HeyTapJob {

	@Inject
	private HeyTapLogic heyTapLogic;
	@Inject
	private HeyTapService heyTapService;

	@Cron("At::d::06:51:12")
	@Transactional
	public void heyTapSign(){
		List<HeyTapEntity> list = heyTapService.findAll();
		for (HeyTapEntity heyTapEntity : list) {
			try {
				Result<Void> result = heyTapLogic.sign(heyTapEntity);
				if (result.isFailure()){
					QqEntity qqEntity = heyTapEntity.getQqEntity();
					if (qqEntity.getPassword() != null && !"".equals(qqEntity.getPassword())){
						Result<HeyTapEntity> loginResult = heyTapLogic.loginByQqPassword(qqEntity.getQq(), qqEntity.getPassword());
						if (loginResult.isFailure()){
							BotUtils.sendMessage(qqEntity,
									"您的欢太账号cookie已失效，请重新绑定！");
							heyTapService.delete(heyTapEntity);
							continue;
						}else {
							HeyTapEntity newEntity = loginResult.getData();
							heyTapEntity.setCookie(newEntity.getCookie());
							heyTapService.save(heyTapEntity);
							heyTapLogic.sign(heyTapEntity);
						}
					}

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

	@Cron("At::d::00:01:54")
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

	@Cron("At::d::19:30:10")
	@Transactional
	public void heyTapEarlySign(){
		List<HeyTapEntity> list = heyTapService.findByEarlyToBedClock(true);
		for (HeyTapEntity heyTapEntity : list) {
			try {
				Result<Void> result = heyTapLogic.earlyBedRegistration(heyTapEntity);
				if (result.isFailure()){
					QqEntity qqEntity = heyTapEntity.getQqEntity();
					BotUtils.sendMessage(qqEntity,
							"您的欢太商城早睡打卡失败，请去手动打卡，24点之前未打卡将损失500积分！");
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}


}
