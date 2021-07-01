package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.controller.QQController;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.QQEntity;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.QQService;

import javax.inject.Inject;

@GroupController
@SuppressWarnings("unused")
public class MyQQController extends QQController {

    @Inject
    private QQService qqService;
    @Inject
    private GroupService groupService;
    @Inject
    private TwitterLogic twitterLogic;

    @Before
    public void before(long qq, long group, BotActionContext actionContext){
        QQEntity qqEntity = qqService.findByQQAndGroup(qq, group);
        if (qqEntity == null) {
            GroupEntity groupEntity = groupService.findByGroup(group);
            if (groupEntity == null) groupEntity = new GroupEntity(group);
            qqEntity = new QQEntity(qq, groupEntity);
        }
        actionContext.set("qqEntity", qqEntity);
    }

    @Action("查询违规")
    @QMsg(at = true)
    public String queryVio(QQEntity qqEntity){
        Integer num = qqEntity.getViolationCount();
        if (num == null) num = 0;
        return "您在本群违规次数为" + num + "次";
    }

    @Action("loc推送 {status}")
    @QMsg(at = true)
    public String locPush(QQEntity qqEntity, boolean status){
        qqEntity.setHostLocPush(status);
        qqService.save(qqEntity);
        if (status) return "hostLoc私聊推送已开启！！";
        else return "hostLoc私聊推送已关闭！！";
    }
}
