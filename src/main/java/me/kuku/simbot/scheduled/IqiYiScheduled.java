package me.kuku.simbot.scheduled;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.IqiYiEntity;
import me.kuku.simbot.entity.IqiYiService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.IqiYiLogic;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class IqiYiScheduled {

	@Resource
	private IqiYiService iqiYiService;
	@Resource
	private IqiYiLogic iqiYiLogic;


	@Scheduled(cron = "51 13 */1 * * ?")
	public void sign(){
		List<IqiYiEntity> list = iqiYiService.findAll();
		for (IqiYiEntity iqiYiEntity : list) {
			try {
				Result<Void> result = iqiYiLogic.sign(iqiYiEntity);
				if (result.isFailure()) {
					QqEntity qqEntity = iqiYiEntity.getQqEntity();
					BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(), "您的爱奇艺cookie已失效，请重新绑定！");
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
