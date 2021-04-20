package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.NeTeaseEntity;
import me.kuku.yuq.logic.NeTeaseLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.NeTeaseService;

import javax.inject.Inject;
import java.io.IOException;

@GroupController
@SuppressWarnings("unused")
public class NeTeaseController {

    @Inject
    private NeTeaseLogic neTeaseLogic;
    @Inject
    private NeTeaseService neTeaseService;

    @Before
    public NeTeaseEntity before(long qq){
        NeTeaseEntity neTeaseEntity = neTeaseService.findByQQ(qq);
        if (neTeaseEntity == null) throw FunKt.getMif().at(qq).plus("您还未绑定网易账号！如需绑定，请私聊机器人发送网易").toThrowable();
        else return neTeaseEntity;
    }

    @Action("网易加速")
    @QMsg(at = true)
    public String listeningVolume(NeTeaseEntity neTeaseEntity) throws IOException {
        String signResult = neTeaseLogic.sign(neTeaseEntity);
        String listeningVolume = neTeaseLogic.listeningVolume(neTeaseEntity);
        return "网易音乐签到：" + signResult + "\n听歌量：" + listeningVolume;
    }

    @PrivateController
    public static class BindNeTeaseController {
        @Inject
        private NeTeaseLogic neTeaseLogic;
        @Inject
        private NeTeaseService neTeaseService;

        @Action("网易")
        public String bindNeTease(ContextSession session, Contact qq) throws IOException {
            qq.sendMessage(Message.Companion.toMessage("请输入网易云音乐账号！！"));
            Message accountMessage = session.waitNextMessage();
            String account = Message.Companion.firstString(accountMessage);
            qq.sendMessage(Message.Companion.toMessage("请输入网易云音乐密码，密码必须为32位md5，不可以传入明文"));
            qq.sendMessage(Message.Companion.toMessage("md5在线加密网站：https://md5jiami.51240.com/，请使用32位小写！！"));
            Message pwdMessage = session.waitNextMessage(60 * 1000 * 2);
            String password = Message.Companion.firstString(pwdMessage);
            Result<NeTeaseEntity> result = neTeaseLogic.loginByPhone(account, password);
            NeTeaseEntity newNeTeaseEntity = result.getData();
            if (newNeTeaseEntity == null) return "绑定失败！！" + result.getMessage();
            NeTeaseEntity neTeaseEntity = neTeaseService.findByQQ(qq.getId());
            if (neTeaseEntity == null) neTeaseEntity = new NeTeaseEntity(qq.getId());
            newNeTeaseEntity.setId(neTeaseEntity.getId());
            newNeTeaseEntity.setQq(neTeaseEntity.getQq());
            neTeaseService.save(newNeTeaseEntity);
            return "绑定网易云音乐账号成功！！";
        }
    }
}
