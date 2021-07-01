package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.HostLocEntity;
import me.kuku.yuq.entity.QQEntity;
import me.kuku.yuq.logic.HostLocLogic;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.HostLocService;
import me.kuku.yuq.service.QQService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JobCenter
@SuppressWarnings("unused")
public class HostLocJob {

    @Inject
    private HostLocService hostLocService;
    @Inject
    private HostLocLogic hostLocLogic;
    @Inject
    private GroupService groupService;
    @Inject
    private QQService qqService;

    private int locId = 0;

    @Cron("At::d::07:00")
    public void sign(){
        List<HostLocEntity> list = hostLocService.findAll();
        for (HostLocEntity hostLocEntity: list){
            String msg = null;
            try {
                boolean isLogin = hostLocLogic.isLogin(hostLocEntity.getCookie());
                if (isLogin){
                    hostLocLogic.sign(hostLocEntity.getCookie());
                }else{
                    Result<String> result = hostLocLogic.login(hostLocEntity.getUsername(), hostLocEntity.getPassword());
                    if (result.getCode() == 200){
                        hostLocEntity.setCookie(result.getData());
                        hostLocService.save(hostLocEntity);
                        hostLocLogic.sign(hostLocEntity.getCookie());
                    }else {
                        msg = result.getMessage();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                msg = "请在群发送<loc签到>进行手动签到！！";
            }
            if (msg != null){
                YuQ yuq = FunKt.getYuq();
                Message message = Message.Companion.toMessage("签到失败了，" + msg);
                if (hostLocEntity.getGroup() == null){
                    yuq.getFriends().get(hostLocEntity.getQq()).sendMessage(message);
                }else {
                    yuq.getGroups().get(hostLocEntity.getGroup()).get(hostLocEntity.getQq()).sendMessage(message);
                }
            }
        }
    }

    @Cron("1m")
    public void locMonitor() {
        List<Map<String, String>> list = hostLocLogic.post();
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
        List<QQEntity> qqList = qqService.findByHostLocPush(true);
        newList.forEach( locMap -> {
            String str = "Loc有新帖了！！" + "\n" +
                    "标题：" + locMap.get("title") + "\n" +
                    "昵称：" + locMap.get("name") + "\n" +
                    "链接：" + locMap.get("url");
            Message message = Message.Companion.toMessage(str);
            for (GroupEntity groupEntity: groupList){
                FunKt.getYuq().getGroups().get(groupEntity.getGroup()).sendMessage(
                        message
                );
            }
            for (QQEntity qqEntity: qqList){
                FunKt.getYuq().getGroups().get(qqEntity.getGroupEntity().getGroup()).get(qqEntity.getQq()).sendMessage(
                        message
                );
            }
        });
    }

}
