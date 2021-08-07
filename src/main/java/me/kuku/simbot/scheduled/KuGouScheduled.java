package me.kuku.simbot.scheduled;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.KuGouEntity;
import me.kuku.simbot.entity.KuGouService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.KuGouLogic;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class KuGouScheduled {

	@Resource
	private KuGouService kuGouService;
	@Resource
	private KuGouLogic kuGouLogic;

	@Scheduled(cron = "17 42 5 * * ?")
	@Transactional
	public void sign(){
		List<KuGouEntity> list = kuGouService.findAll();
		for (KuGouEntity kuGouEntity : list) {
			try {
				Result<Void> result = kuGouLogic.musicianSign(kuGouEntity);
				if (result.isFailure()){
					QqEntity qqEntity = kuGouEntity.getQqEntity();
					BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(), "您的酷狗音乐人自动签到失败，可能为cookie已失效，如需自动签到，请重新绑定！");
					kuGouService.delete(kuGouEntity);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

}
