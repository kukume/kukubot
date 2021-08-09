package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.IqiYiEntity;
import me.kuku.yuq.entity.IqiYiService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.logic.IqiYiLogic;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.util.List;

@JobCenter
public class IqiYiJob {

	@Inject
	private IqiYiService iqiYiService;
	@Inject
	private IqiYiLogic iqiYiLogic;


	@Cron("1h")
	public void sign(){
		List<IqiYiEntity> list = iqiYiService.findAll();
		for (IqiYiEntity iqiYiEntity : list) {
			try {
				Result<Void> result = iqiYiLogic.sign(iqiYiEntity);
				if (result.isFailure()) {
					QqEntity qqEntity = iqiYiEntity.getQqEntity();
					BotUtils.sendMessage(qqEntity, "您的爱奇艺cookie已失效，请重新绑定！");
					iqiYiService.delete(iqiYiEntity);
					continue;
				}
				iqiYiLogic.task(iqiYiEntity);
				iqiYiLogic.draw(iqiYiEntity);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

}
