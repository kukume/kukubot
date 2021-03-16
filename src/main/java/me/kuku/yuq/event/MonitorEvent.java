package me.kuku.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.event.GroupRecallEvent;
import com.icecreamqaq.yuq.event.SendMessageEvent;
import com.icecreamqaq.yuq.message.*;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.MessageEntity;
import me.kuku.yuq.entity.RecallEntity;
import me.kuku.yuq.logic.AILogic;
import me.kuku.yuq.logic.TeambitionLogic;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.TeambitionPojo;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.MessageService;
import me.kuku.yuq.service.RecallService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.ExecutorUtils;
import me.kuku.yuq.utils.OkHttpUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
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
    @Inject
    private TeambitionLogic teambitionLogic;
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
        ExecutorUtils.execute(() -> {
            ConfigEntity configEntity = configService.findByType(ConfigType.Teambition.getType());
            if (configEntity != null){
                uploadToTeam(e.getMessage(), configEntity, e.getGroup());
            }
        });
    }

    @Event
    public void saveMessageMy(SendMessageEvent.Post e){
        Message message = e.getMessage();
        try {
            if (e.getSendTo() instanceof Group) {
                Group group = (Group) e.getSendTo();
                ExecutorUtils.execute(() -> {
                    ConfigEntity configEntity = configService.findByType(ConfigType.Teambition.getType());
                    if (configEntity != null) {
                        uploadToTeam(e.getMessage(), configEntity, group);
                    }
                });
            }
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
                byte[] bytes = OkHttpUtils.getBytes(api + "/tool/urlToPic?url=" + URLEncoder.encode(url, "utf-8"));
                e.getSendTo().sendMessage(FunKt.getMif().imageByByteArray(bytes).toMessage());
            } catch (Exception iex) {
                e.getSendTo().sendMessage(BotUtils.toMessage("转换图片失败，完蛋！！"));
            }
        }
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


    private void uploadToTeam(Message message, ConfigEntity configEntity, Group group){
        ArrayList<MessageItem> list = message.getBody();
        GroupEntity groupEntity = groupService.findByGroup(group.getId());
        LocalDate localDate = LocalDate.now();
        String year = String.valueOf(localDate.getYear());
        String month = String.valueOf(localDate.getMonth().getValue());
        String day = String.valueOf(localDate.getDayOfMonth());
        for (MessageItem item: list){
            if (item instanceof Image){
                Image image = (Image) item;
                String url = image.getUrl();
                String id = image.getId();
                JSONObject jsonObject = configEntity.getContentJsonObject();
                String projectName = jsonObject.getString("project");
                TeambitionPojo teambitionPojo = TeambitionPojo.fromConfig(jsonObject);
                try {
                    byte[] bytes = OkHttpUtils.getBytes(url);
                    Result<String> result = teambitionLogic.uploadToProject(teambitionPojo, bytes,
                            "qqpic", year, month, day, id);
                    if (teambitionPojo.getPanRootId() != null)
                        teambitionLogic.panUploadFile(teambitionPojo, bytes,
                                "qqpic", year, month, day, id);
                    if (result.isFailure()){
                        boolean b = true;
                        if (result.getCode() == 501){
                            Result<TeambitionPojo> loginResult = teambitionLogic.login(jsonObject.getString("phone"),
                                    jsonObject.getString("password"));
                            if (loginResult.isFailure()) b = false;
                            else {
                                TeambitionPojo pojo = loginResult.getData();
                                String cookie = pojo.getCookie();
                                String auth = pojo.getStrikerAuth();
                                jsonObject.put("cookie", cookie);
                                jsonObject.put("auth", auth);
                                configEntity.setContentJsonObject(jsonObject);
                                configService.save(configEntity);
                            }
                        }else if (result.getCode() == 502){
                            Result<TeambitionPojo> loginResult = teambitionLogic.getAuth(teambitionPojo);
                            if (loginResult.isFailure()) b = false;
                            else {
                                TeambitionPojo pojo = loginResult.getData();
                                String auth = pojo.getStrikerAuth();
                                jsonObject.put("auth", auth);
                                configEntity.setContentJsonObject(jsonObject);
                                configService.save(configEntity);
                            }
                        }else b = false;
                        if (b){
                            result = teambitionLogic.uploadToProject(teambitionPojo, bytes,
                                    "qqpic", year, month, day, id);
                            if (teambitionPojo.getPanRootId() != null)
                                teambitionLogic.panUploadFile(teambitionPojo, bytes,
                                        "qqpic", year, month, day, id);
                        }else return;
                    }
                    if (result.isSuccess()){
                        String path = "qqpic/" + year + "/" + month + "/" + day + "/" + id;
                        if (groupEntity.getUploadPicNotice() != null && groupEntity.getUploadPicNotice()){
                            String resultUrl = api + "/teambition/project/" +
                                    jsonObject.getString("name") + "/" + path;
                            Message sendMessage = FunKt.getMif().imageById(id).plus(
                                    "\nTeambition的project链接：\n" + resultUrl);
                            if (teambitionPojo.getPanRootId() != null) {
                                String resultPanUrl = api + "/teambition/pan/" +
                                        jsonObject.getString("name") + "/" + path;
                                sendMessage = sendMessage.plus("\nTeambition的pan链接：\n" + resultPanUrl);
                            }
                            group.sendMessage(sendMessage);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
