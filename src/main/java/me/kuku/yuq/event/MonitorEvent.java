package me.kuku.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.event.GroupRecallEvent;
import com.icecreamqaq.yuq.event.SendMessageEvent;
import com.icecreamqaq.yuq.message.*;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.MessageEntity;
import me.kuku.yuq.entity.RecallEntity;
import me.kuku.yuq.logic.AILogic;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.MessageService;
import me.kuku.yuq.service.RecallService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EventListener
@SuppressWarnings("unused")
public class MonitorEvent {
    @Inject
    private GroupService groupService;
    @Inject
    private MessageService messageService;
    @Inject
    private RecallService recallService;
    @Inject
    private AILogic AILogic;

    @Event
    public void saveMessageGroup(GroupMessageEvent e){
        messageService.save(
                new MessageEntity(null, e.getMessage().source.getId(), e.getGroup().getId(), e.getSender().getId(),
                        BotUtils.messageToJsonArray(e.getMessage()).toString(), new Date())
        );
    }

    @Event
    public void saveMessageMy(SendMessageEvent.Post e){
        messageService.save(
                new MessageEntity(null, e.getMessageSource().getId(), e.getSendTo().getId(),
                        FunKt.getYuq().getBotId(), BotUtils.messageToJsonArray(e.getMessage()).toString(), new Date())
        );
    }

    @Event
    public void readMessage(GroupMessageEvent e) throws IOException {
        Message message = e.getMessage();
        MessageSource reply = message.getReply();
        List<String> list = message.toPath();
        String lastPath = list.get(list.size() - 1);
        if (reply != null && lastPath.endsWith("读消息")){
            MessageEntity messageEntity = messageService.findByMessageId(reply.getId());
            String msg;
            if (messageEntity == null){
                msg = "找不到您当前回复的消息！！";
            }else {
                JSONArray jsonArray = messageEntity.getContentJsonArray();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sb.append("类型：").append(jsonObject.getString("type")).append("\n");
                    sb.append("内容：").append(jsonObject.getString("content")).append("\n");
                    sb.append("=================").append("\n");
                }
                msg = sb.deleteCharAt(sb.length() - 1).toString();
            }
            e.getGroup().sendMessage(Message.Companion.toMessage(msg));
        }
        MessageItem iat = message.getBody().get(0);
        if (iat instanceof At){
            At at = (At) iat;
            if (at.getUser() == FunKt.getYuq().getBotId()){
                StringBuilder sb = new StringBuilder();
                for (MessageItem messageItem : message.getBody()) {
                    if (messageItem instanceof Text){
                        Text text = (Text) messageItem;
                        String textStr = text.getText();
                        textStr = textStr.trim();
                        if ("读消息".equals(textStr)) return;
                        sb.append(textStr);
                    }
                }
                String textChat = AILogic.textChat(sb.toString(), String.valueOf(e.getSender().getId()));
                e.getGroup().sendMessage(FunKt.getMif().at(e.getSender().getId()).plus(textChat));
            }
        }
    }

    @Event
    public void recallEvent(GroupRecallEvent e){
        long qq = e.getSender().getId();
        MessageEntity messageEntity = messageService.findByMessageId(e.getMessageId());
        if (messageEntity == null) return;
        RecallEntity recallEntity = new RecallEntity(null, qq, e.getGroup().getId(), messageEntity, new Date());
        recallService.save(recallEntity);
        GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
        if (groupEntity == null) return;
        if (Boolean.valueOf(true).equals(groupEntity.getRecall())){
            if (!e.getSender().equals(e.getOperator())) return;
            e.getGroup().sendMessage(FunKt.getMif().text("群成员").plus(FunKt.getMif().at(qq)).plus("\n妄图撤回一条消息。\n消息内容为：\n")
                    .plus(BotUtils.jsonArrayToMessage(messageEntity.getContentJsonArray())));
        }
    }

    @Event
    public void flashNotify(GroupMessageEvent e){
        long group = e.getGroup().getId();
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) return;
        if (Boolean.valueOf(true).equals(groupEntity.getFlashNotify())){
            ArrayList<MessageItem> body = e.getMessage().getBody();
            long qq = e.getSender().getId();
            for (MessageItem item : body) {
                if (item instanceof FlashImage){
                    FlashImage fl = (FlashImage) item;
                    Message msg = FunKt.getMif().text("群成员：").plus(FunKt.getMif().at(qq))
                            .plus("\n妄图发送闪照：\n")
                            .plus(FunKt.getMif().imageByUrl(fl.getUrl()));
                    e.getGroup().sendMessage(msg);
                }
            }
        }
    }
}
