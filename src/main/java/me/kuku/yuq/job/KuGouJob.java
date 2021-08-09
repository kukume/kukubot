package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.KuGouEntity;
import me.kuku.yuq.entity.KuGouService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.logic.KuGouLogic;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.util.List;

@JobCenter
public class KuGouJob {

	@Inject
	private KuGouService kuGouService;
	@Inject
	private KuGouLogic kuGouLogic;

	@Cron("At::d::05:42:17")
	@Transactional
	public void sign(){
		List<KuGouEntity> list = kuGouService.findAll();
		for (KuGouEntity kuGouEntity : list) {
			try {
				Result<Void> result = kuGouLogic.musicianSign(kuGouEntity);
				if (result.isFailure()){
					QqEntity qqEntity = kuGouEntity.getQqEntity();
					BotUtils.sendMessage(qqEntity, "您的酷狗音乐人自动签到失败，可能为cookie已失效，如需自动签到，请重新绑定！");
					kuGouService.delete(kuGouEntity);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

}
