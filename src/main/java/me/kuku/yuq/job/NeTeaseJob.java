package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.NeTeaseEntity;
import me.kuku.yuq.logic.NeTeaseLogic;
import me.kuku.yuq.service.NeTeaseService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@JobCenter
@SuppressWarnings("unused")
public class NeTeaseJob {
    @Inject
    private NeTeaseLogic neTeaseLogic;
    @Inject
    private NeTeaseService neTeaseService;

    @Cron("At::d::09:00")
    public void ne(){
        List<NeTeaseEntity> list = neTeaseService.findAll();
        list.forEach(neTeaseEntity -> {
            try {
                neTeaseLogic.sign(neTeaseEntity);
                neTeaseLogic.listeningVolume(neTeaseEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
