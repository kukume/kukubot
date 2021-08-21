package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.BaiduEntity;
import me.kuku.yuq.entity.BaiduService;
import me.kuku.yuq.logic.BaiduLogic;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
                for (int i = 0; i < 12; i++) {
                    TimeUnit.SECONDS.sleep(30);
                    baiduLogic.ybbSign(baiduEntity);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Cron("At::d::04:12:51")
    public void tieBaSign(){
        List<BaiduEntity> list = baiduService.findAll();
        for (BaiduEntity baiduEntity : list) {
            try {
                baiduLogic.tieBaSign(baiduEntity);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
