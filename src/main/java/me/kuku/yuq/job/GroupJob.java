package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yuq.FunKt;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.service.GroupService;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@JobCenter
@SuppressWarnings("unused")
public class GroupJob {
    @Inject
    private GroupService groupService;

    @Cron("At::h::00")
    public void onTimeAlarm(){
        List<GroupEntity> list = groupService.findByOnTimeAlarm(true);
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(new Date()));
        if (hour == 0) hour = 12;
        if (hour > 12) hour -= 12;
        String url = "https://share.kuku.me/189/kuku/bot/time/" + hour + ".jpg";
        list.forEach(groupEntity ->
                FunKt.getYuq().getGroups().get(groupEntity.getGroup())
                .sendMessage(FunKt.getMif().imageByUrl(url).toMessage())
        );
    }
}
