package me.kuku.yuq.controller.hostloc;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Member;
import me.kuku.yuq.entity.HostLocEntity;
import me.kuku.yuq.logic.HostLocLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.HostLocService;

import javax.inject.Inject;
import java.io.IOException;

@PrivateController
public class HostLocLoginController {

    @Inject
    private HostLocLogic hostLocLogic;
    @Inject
    private HostLocService hostLocService;

    @Action("loc {username} {password}")
    public String login(String username, String password, Contact qq){
        Result<String> result;

        try {
            result = hostLocLogic.login(username, password);
        } catch (IOException e) {
            e.printStackTrace();
            return "网络链接失败，请稍后再试！！";
        }
        if (result.getCode() == 200){
            String cookie = result.getData();
            HostLocEntity hostLocEntity = hostLocService.findByQQ(qq.getId());
            if (hostLocEntity == null) hostLocEntity = new HostLocEntity(qq.getId());
            if (qq instanceof Member){
                Member member = (Member) qq;
                hostLocEntity.setGroup(member.getGroup().getId());
            }
            hostLocEntity.setUsername(username);
            hostLocEntity.setPassword(password);
            hostLocEntity.setCookie(cookie);
            hostLocService.save(hostLocEntity);
            return "绑定或者更新hostloc信息成功！！";
        }else return result.getMessage();
    }
}
