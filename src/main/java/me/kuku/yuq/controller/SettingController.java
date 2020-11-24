package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.mirai.MiraiBot;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQLoginLogic;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@PrivateController
public class SettingController extends QQController {
    @Inject
    private GroupService groupService;
    @Config("YuQ.Mirai.bot.master")
    private String master;
    @Inject
    private QQLoginLogic qqLoginLogic;
    @Inject
    private OkHttpWebImpl web;
    @Inject
    private MiraiBot miraiBot;
    @Inject
    private ConfigService configService;

    @Before
    public void before(long qq, BotActionContext actionContext){
        if (qq != Long.parseLong(master))
            throw Message.Companion.toMessage("您不是机器人主人，无法执行！！").toThrowable();
        actionContext.set("qqLoginEntity", BotUtils.toQQLoginEntity(web, miraiBot));
    }

    @Action("群开启 {groupNo}")
    @Synonym({"群关闭 {groupNo}"})
    public String groupOpen(long groupNo, @PathVar(0) String str){
        Map<Long, Group> groups = FunKt.getYuq().getGroups();
        if (groups.containsKey(groupNo)){
            GroupEntity groupEntity = groupService.findByGroup(groupNo);
            if (groupEntity == null) groupEntity = new GroupEntity(groupNo);
            groupEntity.setStatus(str.contains("开启"));
            groupService.save(groupEntity);
            return "机器人开启或者关闭成功！！";
        }else return "机器人并没有加入这个群！！";
    }

    @Action("同意入群 {groupNo}")
    public String agreeAddGroup(QQLoginEntity qqLoginEntity, long groupNo) throws IOException {
        List<Map<String, String>> groupMsgList = qqLoginLogic.getGroupMsgList(qqLoginEntity);
        AtomicReference<Map<String, String>> mm = new AtomicReference<>();
        for (Map<String, String> map: groupMsgList){
            if (String.valueOf(groupNo).equals(map.get("group"))){
                mm.set(map);
                break;
            }
        }
        if (mm.get() == null) return "没有找到这个群号";
        return qqLoginLogic.operatingGroupMsg(qqLoginEntity, "agree", mm.get(), null);
    }

    @Action("退群 {groupNo}")
    public String leaveGroup(long groupNo){
        Map<Long, Group> groups = FunKt.getYuq().getGroups();
        if (groups.containsKey(groupNo)){
            groups.get(groupNo).leave();
            return "退出群聊成功！！";
        }else return "机器人并没有加入这个群！！";
    }

    @Action("qqai")
    public String settingQQAI(ContextSession session){
        reply("将设置QQAI的信息，请确保您的应用赋予了图片鉴黄、智能闲聊、通用OCR能力");
        reply("请输入ai.qq.com/v1的appId");
        Message firstMessage = session.waitNextMessage();
        String appId = Message.Companion.firstString(firstMessage);
        reply("请输入ai.qq.com/v1的appKey");
        Message secondMessage = session.waitNextMessage();
        String appKey = Message.Companion.firstString(secondMessage);
        ConfigEntity configEntity1 = configService.findByType("qqAIAppId");
        if (configEntity1 == null) configEntity1 = new ConfigEntity("qqAIAppId");
        ConfigEntity configEntity2 = configService.findByType("qqAIAppId");
        if (configEntity2 == null) configEntity2 = new ConfigEntity("qqAIAppKey");
        configEntity1.setContent(appId);
        configEntity2.setContent(appKey);
        configService.save(configEntity1);
        configService.save(configEntity2);
        return "绑定qqAI的信息成功！！";
    }

    @Action("lolicon {apiKey}")
    public String settingLoLiCon(String apiKey){
        ConfigEntity configEntity = configService.findByType("loLiCon");
        if (configEntity == null) configEntity = new ConfigEntity("loLiCon");
        configEntity.setContent(apiKey);
        configService.save(configEntity);
        return "绑定loLiCon的apiKey成功！！";
    }
}
