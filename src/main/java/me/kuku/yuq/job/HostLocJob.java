package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.HostLocEntity;
import me.kuku.yuq.logic.HostLocLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.HostLocService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@JobCenter
public class HostLocJob {

    @Inject
    private HostLocService hostLocService;
    @Inject
    private HostLocLogic hostLocLogic;

    @Cron("At::d::07")
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

}
