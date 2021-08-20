package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.BaiduEntity;
import me.kuku.yuq.entity.BaiduService;
import me.kuku.yuq.logic.BaiduLogic;

import javax.inject.Inject;
import java.util.List;

@JobCenter
public class BaiduJob {

    @Inject
    private BaiduService baiduService;
    @Inject
    private BaiduLogic baiduLogic;

    @Cron("At::d::07:41:23")
    public void ybbSign(){
        List<BaiduEntity> list = baiduService.findAll();
        for (BaiduEntity baiduEntity : list) {
            try {
                baiduLogic.ybbSign(baiduEntity);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
