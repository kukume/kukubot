package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import me.kuku.yuq.entity.HostLocEntity;
import me.kuku.yuq.logic.HostLocLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.HostLocService;

import javax.inject.Inject;
import java.io.IOException;

@GroupController
@SuppressWarnings("unused")
public class HostLocController {

    @Inject
    private HostLocService hostLocService;
    @Inject
    private HostLocLogic hostLocLogic;

    @Before
    public HostLocEntity before(long qq){
        HostLocEntity hostLocEntity = hostLocService.findByQQ(qq);
        if (hostLocEntity == null) throw FunKt.getMif().text("您还未绑定hostloc账号，请私聊机器人发送<loc 账号 密码>进行绑定").toMessage().toThrowable();
        else return hostLocEntity;
    }

    @Action("loc签到")
    @QMsg(at = true)
    public String locSign(HostLocEntity hostLocEntity, long qq, Group group) throws IOException {
        group.sendMessage(FunKt.getMif().at(qq).plus("请稍后，正在为您签到中！！！"));
        String cookie = hostLocEntity.getCookie();
        boolean isLogin = hostLocLogic.isLogin(cookie);
        if (isLogin) hostLocLogic.sign(cookie);
        else {
            Result<String> result = hostLocLogic.login(hostLocEntity.getUsername(), hostLocEntity.getPassword());
            if (result.getCode() == 200){
                hostLocEntity.setCookie(result.getData());
                hostLocService.save(hostLocEntity);
                hostLocLogic.sign(cookie);
            }else {
                return "签到失败，" + result.getMessage();
            }
        }
        return "签到成功！！";
    }

    @PrivateController
    public static class HostLocLoginController{
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
}
