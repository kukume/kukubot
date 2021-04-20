package me.kuku.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Config;
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
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.MessageService;
import me.kuku.yuq.service.RecallService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URLEncoder;
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
    private ConfigService configService;
    @Inject
    private ToolLogic toolLogic;
    @Config("YuQ.Mirai.bot.api")
    private String api;

    @Event
    public void saveMessageGroup(GroupMessageEvent e){
        messageService.save(
                new MessageEntity(null, e.getMessage().source.getId(), e.getGroup().getId(), e.getSender().getId(),
                        BotUtils.messageToJsonArray(e.getMessage()).toString(), new Date())
        );
    }

    @Event
    public void saveMessageMy(SendMessageEvent.Post e){
        Message message = e.getMessage();
        try {
            int id = e.getMessageSource().getId();
            messageService.save(
                    new MessageEntity(null, id, e.getSendTo().getId(),
                            FunKt.getYuq().getBotId(), BotUtils.messageToJsonArray(message).toString(), new Date())
            );
        } catch (IndexOutOfBoundsException ex) {
            e.getSendTo().sendMessage(BotUtils.toMessage("消息被屏蔽，正在把文字转换成图片中，请稍后！！！"));
            StringBuilder sb = new StringBuilder();
            for (MessageItem item : message.getBody()) {
                if (item instanceof Text){
                    sb.append(((Text) item).getText()).append(" ");
                }
            }
            try {
                String url = api + "/tool/word?word=" + URLEncoder.encode(sb.toString(), "utf-8");
//                String picUrl = toolLogic.urlToPic(url);
//                e.getSendTo().sendMessage(FunKt.getMif().imageByUrl(picUrl).toMessage());
                InputStream is = OkHttpUtils.getByteStream(api + "/tool/urlToPic?url=" + URLEncoder.encode(url, "utf-8"));
                e.getSendTo().sendMessage(FunKt.getMif().imageByInputStream(is).toMessage());
            } catch (Exception iex) {
                e.getSendTo().sendMessage(BotUtils.toMessage("转换图片失败，完蛋！！"));
            }
        }
    }

    @Event
    public void readMessage(GroupMessageEvent e){
        Message message = e.getMessage();
        MessageSource reply = message.getReply();
        List<String> list = message.toPath();
        if (list.size() == 0) return;
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
            e.getGroup().sendMessage(FunKt.getMif().text("群成员").plus(FunKt.getMif().at(qq)).plus("\n妄图撤回一条消息。\n消息内容为："));
            e.getGroup().sendMessage(BotUtils.jsonArrayToMessage(messageEntity.getContentJsonArray()));
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
