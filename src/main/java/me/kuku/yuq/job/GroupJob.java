package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.DateTimeFormatterUtils;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@JobCenter
@SuppressWarnings("unused")
public class GroupJob {
    @Inject
    private GroupService groupService;

    @Cron("At::h::00")
    public void onTimeAlarm(){
        List<GroupEntity> list = groupService.findByOnTimeAlarm(true);
        String hourStr = DateTimeFormatterUtils.formatNow("HH");
        int hour = Integer.parseInt(hourStr);
        if (hour == 0) hour = 12;
        if (hour > 12) hour -= 12;
        String name = hour + ".jpg";
        File file = new File("hour" + File.separator + name);
        Message message;
        if (file.exists()){
            message = FunKt.getMif().imageByFile(file).toMessage();
        }else {
            String url = "https://file.kuku.me/time/hour/" + name;
            message = FunKt.getMif().imageByUrl(url).toMessage();
        }
        list.forEach(groupEntity ->
                FunKt.getYuq().getGroups().get(groupEntity.getGroup())
                .sendMessage(message)
        );
    }
}
