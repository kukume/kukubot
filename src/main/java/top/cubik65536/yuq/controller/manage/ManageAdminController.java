package top.cubik65536.yuq.controller.manage;
import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Member;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.service.GroupService;

import javax.inject.Inject;

/**
 * ManageAdminController
 * top.cubik65536.yuq.controller.manage
 * CubikBot
 * <p>
 * Created by Cubik65536 on 2021-03-21.
 * Copyright © 2020-2021 Cubik Inc. All rights reserved.
 * <p>
 * Description:
 * History:
 * 1. 2021-03-21 [Cubik65536]: Create file ManageAdminController;
 */

@GroupController
@SuppressWarnings("unused")
public class ManageAdminController {
    @Config("YuQ.Mirai.bot.master")
    private String master;
    @Inject
    private GroupService groupService;

    @Before
    public GroupEntity before(Member qq, long group){
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) groupEntity = new GroupEntity(group);
        if (groupEntity.isAdmin(qq.getId()) || groupEntity.isSuperAdmin(qq.getId())
                || qq.getId() == Long.parseLong(master) || (qq.isAdmin() && Boolean.valueOf(true).equals(groupEntity.getGroupAdminAuth()))){
            return groupEntity;
        }else throw FunKt.getMif().at(qq).plus("您的权限不足，无法执行！！").toThrowable();
    }

    @Action("聊天AI切换 {type}")
    @Synonym("聊天切换 {type}")
    @QMsg(at = true)
    public String chatAiType(GroupEntity groupEntity, String type){
        String chatAiType;
        if ("QingYunKe".equals(type) || "HaiZhi".equals(type)){
            chatAiType = type;
        } else return "没有该类型，请重试！！";
        groupEntity.setChatAiType(chatAiType);
        groupService.save(groupEntity);
        return "聊天AI切换成" + type + "成功！！";
    }

}
