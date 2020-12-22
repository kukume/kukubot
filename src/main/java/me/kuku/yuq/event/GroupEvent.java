package me.kuku.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.alibaba.fastjson.JSONArray;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.event.GroupMemberJoinEvent;
import com.icecreamqaq.yuq.event.GroupMemberLeaveEvent;
import com.icecreamqaq.yuq.event.GroupMemberRequestEvent;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.MessageEntity;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.service.DaoService;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.MessageService;
import me.kuku.yuq.service.QQService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@EventListener
@SuppressWarnings("unused")
public class GroupEvent {
    @Inject
    private ToolLogic toolLogic;
    @Inject
    private GroupService groupService;
    @Inject
    private DaoService daoService;
    @Inject
    private QQService qqService;
    @Inject
    private MessageService messageService;

    @Event
    public void groupMemberRequest(GroupMemberRequestEvent e){
        GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
        if (groupEntity == null) return;
        if (groupEntity.getAutoReview() != null && groupEntity.getAutoReview()){
            boolean status = true;
            JSONArray blackJsonArray = groupEntity.getBlackJsonArray();
            for (int i = 0; i < blackJsonArray.size(); i++){
                long black = blackJsonArray.getLong(i);
                if (black == e.getQq().getId()){
                    status = false;
                    break;
                }
            }
            e.setAccept(status);
            e.cancel = true;
        }
    }

    @Event
    public void groupMemberLeave(GroupMemberLeaveEvent.Leave e){
        long qq = e.getMember().getId();
        long group = e.getGroup().getId();
        daoService.delByQQ(qq);
        qqService.delByQQAndGroup(qq, group);
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) return;
        String msg;
        if (groupEntity.getLeaveGroupBlack() != null && groupEntity.getLeaveGroupBlack()){
            JSONArray blackJsonArray = groupEntity.getBlackJsonArray();
            blackJsonArray.add(String.valueOf(qq));
            groupEntity.setBlackJsonArray(blackJsonArray);
            groupService.save(groupEntity);
            msg = "刚刚，" + e.getMember().getName() + "退群了，已加入本群黑名单！！";
        }else msg = "刚刚，" + e.getMember().getName() + "离开了我们！！";
        List<MessageEntity> messageList = messageService.findLastMessage(qq, group);
        Message finallyMessage;
        if (messageList.size() == 0) {
            finallyMessage = Message.Companion.toMessage("他好像还没有说过话！！");
        }
        else {
            MessageEntity messageEntity = messageList.get(0);
            msg += "\n尽管他就这么的走了，但是我们仍然不要忘记他在[" +
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(messageEntity.getDate()) +
                    "]说的最后一句话：";
            finallyMessage = BotUtils.jsonArrayToMessage(messageEntity.getContentJsonArray());
        }
        e.getGroup().sendMessage(Message.Companion.toMessage(msg));
        e.getGroup().sendMessage(finallyMessage);
    }

    @Event
    public void groupMemberKick(GroupMemberLeaveEvent.Kick e){
        long qq = e.getMember().getId();
        long group = e.getGroup().getId();
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) return;
        JSONArray blackJsonArray = groupEntity.getBlackJsonArray();
        blackJsonArray.add(String.valueOf(qq));
        groupEntity.setBlackJsonArray(blackJsonArray);
        groupService.save(groupEntity);
        daoService.delByQQ(qq);
        qqService.delByQQAndGroup(qq, group);
    }

    @Event
    public void groupMemberJoin(GroupMemberJoinEvent e) throws IOException {
        long group = e.getGroup().getId();
        long qq = e.getMember().getId();
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) return;
        if (Boolean.valueOf(true).equals(groupEntity.getWelcomeMsg())){
            e.getGroup().sendMessage(
                    FunKt.getMif().at(qq).plus(
                            "欢迎加入本群\n" +
                                    "您是本群的第" + (e.getGroup().getMembers().size() + 1) + "位成员\n" +
                                    "您可以愉快的与大家交流啦！！"
                    ).plus(FunKt.getMif().imageByUrl(e.getMember().getAvatar()))
                    .plus("一言：" + toolLogic.hiToKoTo().get("text"))
            );
        }
    }
}
