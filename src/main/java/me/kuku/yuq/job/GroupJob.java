package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.OkHttpUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JobCenter
public class GroupJob {
    @Inject
    private GroupService groupService;
    @Inject
    private ToolLogic toolLogic;

    private int locId = 0;

    @Cron("At::h::00")
    public void onTimeAlarm() throws IOException {
        List<GroupEntity> list = groupService.findByOnTimeAlarm(true);
        int hour = Integer.parseInt(OkHttpUtils.getStr("https://api.kuku.me/time/h"));
        if (hour == 0) hour = 12;
        if (hour < 12) hour -= 12;
        String url = "https://u.iheit.com/kuku/bot/time/" + hour + ".jpg";
        list.forEach(groupEntity ->
                FunKt.getYuq().getGroups().get(groupEntity.getGroup())
                .sendMessage(FunKt.getMif().imageByUrl(url).toMessage())
        );
    }

    @Cron("1m")
    public void locMonitor() throws IOException {
        List<Map<String, String>> list = toolLogic.hostLocPost();
        if (list.size() == 0) return;
        List<Map<String, String>> newList = new ArrayList<>();
        if (locId != 0){
            for (Map<String, String> map: list){
                if (Integer.parseInt(map.get("id")) <= locId) break;
                newList.add(map);
            }
        }
        locId = Integer.parseInt(list.get(0).get("id"));
        List<GroupEntity> groupList = groupService.findByLocMonitor(true);
        for (GroupEntity groupEntity: groupList){
            newList.forEach( locMap -> {
                String str = "Loc有新帖了！！" + "\n" +
                        "标题：" + locMap.get("title") + "\n" +
                        "昵称：" + locMap.get("name") + "\n" +
                        "链接：" + locMap.get("url");
                FunKt.getYuq().getGroups().get(groupEntity.getGroup()).sendMessage(
                        Message.Companion.toMessage(str)
                );
            });
        }
    }
}
