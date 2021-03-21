package top.cubik65536.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.entity.GroupEntity;
import top.cubik65536.yuq.logic.ChatLogic;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.GroupService;

import javax.inject.Inject;

/**
 * ChatEvent
 * me.kuku.yuq.event
 * kukubot
 * <p>
 * Created by Cubik65536 on 2021-02-20.
 * Copyright © 2020-2021 Cubik Inc. All rights reserved.
 * <p>
 * Description: 聊天事件监听器
 * History:
 * 1. 2021-02-20 [Cubik65536]: Create file ChatEvent;
 * 2. 2021-02-20 [Cubik65536]: 创建监听事件，"kuku"、"kukubot"、"kkb"开头内容会触发AI聊天;
 */
@EventListener
@SuppressWarnings("unused")
public class ChatEvent {
    @Inject
    private GroupService groupService;
    @Inject
    private ChatLogic chatLogic;
    @Inject
    private ConfigService configService;

    @Event
    public void chat(GroupMessageEvent e) throws Exception {
        GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
        if (groupEntity == null) return;
        Message message = e.getMessage();
        if (message.toPath().size() == 0) return;
        String str;
        try {
            str = Message.Companion.firstString(message);
        } catch (IllegalStateException ex) {
            return;
        }
        String type = groupEntity.getChatAiType();
        if (type == null) return;
        MessageItemFactory mif = FunKt.getMif();
        if (type.equals("QingYunKe")) {
            String key = "free";
            if (message.toPath().get(0).startsWith("kuku")) {
                String msg = message.toPath().get(0).substring("kuku".length());
                JSONObject jsonObject = JSONObject.parseObject(chatLogic.getQingYunKe(key, msg));
                msg = jsonObject.getString("content");
                e.getGroup().sendMessage(mif.text(msg).toMessage());
                return;
            } else if (message.toPath().get(0).startsWith("kukubot")) {
                String msg = message.toPath().get(0).substring("kukubot".length());
                JSONObject jsonObject = JSONObject.parseObject(chatLogic.getQingYunKe(key, msg));
                msg = jsonObject.getString("content");
                e.getGroup().sendMessage(mif.text(msg).toMessage());
                return;
            } else if (message.toPath().get(0).startsWith("kkb")) {
                String msg = message.toPath().get(0).substring("kkb".length());
                JSONObject jsonObject = JSONObject.parseObject(chatLogic.getQingYunKe(key, msg));
                msg = jsonObject.getString("content");
                e.getGroup().sendMessage(mif.text(msg).toMessage());
                return;
            } else return;
        } else if (type.equals("HaiZhi")) {
            ConfigEntity configEntity = configService.findByType("ChatAi");
            if (configEntity == null && message.toPath().get(0).startsWith("kuku") && message.toPath().get(0).startsWith
                    ("kukubot") && message.toPath().get(0).startsWith("kkb")) {
                e.getGroup().sendMessage(mif.text("您还没有配置海知智能机器人的apiKey，无法进行聊天！！").toMessage());
                return;
            }
            String key = configEntity.getContent();
            if (message.toPath().get(0).startsWith("kuku")) {
                String msg = message.toPath().get(0).substring("kuku".length());
                msg = chatLogic.getHaiZhi(key, msg);
                JSONObject jsonObject = JSONObject.parseObject(msg);
                msg = jsonObject.getJSONObject("result")
                        .getJSONArray("intents")
                        .getJSONObject(0)
                        .getJSONArray("outputs")
                        .getJSONObject(1)
                        .getJSONObject("property")
                        .getString("text");
                e.getGroup().sendMessage(mif.text(msg).toMessage());
                return;
            } else if (message.toPath().get(0).startsWith("kukubot")) {
                String msg = message.toPath().get(0).substring("kukubot".length());
                msg = chatLogic.getHaiZhi(key, msg);
                JSONObject jsonObject = JSONObject.parseObject(msg);
                msg = jsonObject.getJSONObject("result")
                        .getJSONArray("intents")
                        .getJSONObject(0)
                        .getJSONArray("outputs")
                        .getJSONObject(1)
                        .getJSONObject("property")
                        .getString("text");
                e.getGroup().sendMessage(mif.text(msg).toMessage());
                return;
            } else if (message.toPath().get(0).startsWith("kkb")) {
                String msg = message.toPath().get(0).substring("kkb".length());
                msg = chatLogic.getHaiZhi(key, msg);
                JSONObject jsonObject = JSONObject.parseObject(msg);
                msg = jsonObject.getJSONObject("result")
                        .getJSONArray("intents")
                        .getJSONObject(0)
                        .getJSONArray("outputs")
                        .getJSONObject(1)
                        .getJSONObject("property")
                        .getString("text");
                e.getGroup().sendMessage(mif.text(msg).toMessage());
                return;
            } else return;
        } else {
            if (message.toPath().get(0).startsWith("kuku") && message.toPath().get(0).startsWith
                    ("kukubot") && message.toPath().get(0).startsWith("kkb")) {
                e.getGroup().sendMessage(mif.text("您还没有配置聊天机器人类型！请选择QingYunKe（不需要申请ApiKey）和HaiZhi（需要申请ApiKey）").toMessage());
            }
            return;
        }

    }
}
