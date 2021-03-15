package me.kuku.yuq.controller.netease;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import me.kuku.yuq.entity.NeTeaseEntity;
import me.kuku.yuq.logic.NeTeaseLogic;
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
}
