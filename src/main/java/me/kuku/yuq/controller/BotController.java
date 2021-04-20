package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.*;
import com.alibaba.fastjson.JSONArray;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Image;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItem;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.BotLogic;
import me.kuku.yuq.logic.QQGroupLogic;
import me.kuku.yuq.logic.QQLoginLogic;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.GroupMember;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.DateTimeFormatterUtils;
import net.mamoe.mirai.contact.PermissionDeniedException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@GroupController
@SuppressWarnings("unused")
public class BotController {
    @Config("YuQ.Mirai.user.qq")
    private String qq;
    @Config("YuQ.Mirai.bot.master")
    private String master;
    @Inject
    private QQLoginLogic qqLoginLogic;
    @Inject
    private ToolLogic toolLogic;
    @Inject
    private QQGroupLogic qqGroupLogic;
    @Inject
    private GroupService groupService;
    @Inject
    private BotLogic botLogic;

    private List<Long> notSpeakByDay = null;
    private List<Long> notSpeakByNever = null;

    @Before
    public void before(BotActionContext actionContext){
        QQLoginEntity qqLoginEntity = botLogic.getQQLoginEntity();
        actionContext.set("qqLoginEntity", qqLoginEntity);
    }

    @Action("qq上传")
    public String groupUpload(QQLoginEntity qqLoginEntity, Long qq, ContextSession session, Group group) throws IOException {
        group.sendMessage(FunKt.getMif().at(qq).plus("请发送您需要上传的图片！！"));
        Message message = session.waitNextMessage();
        ArrayList<MessageItem> body = message.getBody();
        StringBuilder sb = new StringBuilder().append("您上传的图片链接如下：").append("\n");
        int i = 1;
        for (MessageItem item: body){
            if (item instanceof Image){
                Image image = (Image) item;
                Result<Map<String, String>> result = qqLoginLogic.groupUploadImage(qqLoginEntity, image.getUrl());
                String url;
                Map<String, String> map = result.getData();
                if (map == null) url = result.getMessage();
                else url = map.get("picUrl");
                sb.append(i++).append("、").append(url).append("\n");
            }
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Action("查业务 {qqNo}")
    @QMsg(at = true, atNewLine = true)
    public String queryVip(Long qqNo, QQLoginEntity qqLoginEntity) throws IOException {
        return qqLoginLogic.queryFriendVip(qqLoginEntity, qqNo, null);
    }

    @Action("列出{day}天未发言")
    @QMsg(at = true, atNewLine = true)
    public String notSpeak(Long group, String day, QQLoginEntity qqLoginEntity) throws IOException {
        Result<List<GroupMember>> result = qqLoginLogic.groupMemberInfo(qqLoginEntity, group);
        if (result.getCode() == 200){
            List<GroupMember> list = result.getData();
            List<Long> qqList = new ArrayList<>();
            StringBuilder sb = new StringBuilder().append("本群").append(day).append("天未发言的成员如下：").append("\n");
            for (GroupMember groupMember : list) {
                if ((System.currentTimeMillis() - groupMember.getLastTime()) / (1000 * 60 * 60 * 24) > Integer.parseInt(day)){
                    sb.append(groupMember.getQq()).append("\n");
                    qqList.add(groupMember.getQq());
                }
            }
            notSpeakByDay = qqList;
            return sb.deleteCharAt(sb.length() - 1).toString();
        }else return result.getMessage();
    }

    @Action("列出从未发言")
    public String neverSpeak(Long group, QQLoginEntity qqLoginEntity) throws IOException {
        Result<List<GroupMember>> result = qqLoginLogic.groupMemberInfo(qqLoginEntity, group);
        if (result.getCode() == 200){
            List<GroupMember> list = result.getData();
            List<Long> qqList = new ArrayList<>();
            StringBuilder sb = new StringBuilder().append("本群从未发言的成员如下：").append("\n");
            for (GroupMember groupMember : list) {
                if ((groupMember.getLastTime().equals(groupMember.getJoinTime()) || groupMember.getIntegral() <= 1)
                && System.currentTimeMillis() - groupMember.getJoinTime() > 1000 * 60 * 60 * 24){
                    sb.append(groupMember.getQq()).append("\n");
                    qqList.add(groupMember.getQq());
                }
            }
            notSpeakByNever = qqList;
            return sb.deleteCharAt(sb.length() - 1).toString();
        }else return result.getMessage();
    }

    @Action("t未发言")
    @QMsg(at = true)
    public String tNotSpeakByDay(Group group, long qq) throws IOException {
        if (notSpeakByDay == null) return "请先发送<列出x天未发言>！！";
        GroupEntity groupEntity = groupService.findByGroup(group.getId());
        if (groupEntity.isSuperAdmin(qq) || qq == Long.parseLong(master)){
            for (Long innerQQ : notSpeakByDay) {
                try {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    group.get(innerQQ).kick("一定时间内未发言");
                } catch (PermissionDeniedException e) {
                    return "机器人的权限不足，无法执行";
                } catch (Exception e){
                    qqGroupLogic.deleteGroupMember(botLogic.getQQLoginEntity(), qq, group.getId(), true);
                }
            }
            return "踢出成功！！";
        }else return "您的权限不足，无法执行";
    }

    @Action("t从未发言")
    @QMsg(at = true)
    public String tNotSpeakByNever(long qq, Group group){
        if (notSpeakByNever == null) return "请先发送<列出从未发言>！！";
        GroupEntity groupEntity = groupService.findByGroup(group.getId());
        if (groupEntity.isSuperAdmin(qq) || qq == Long.parseLong(master)){
            for (Long innerQQ : notSpeakByNever) {
                try {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    group.get(innerQQ).kick("从未发言");
                } catch (PermissionDeniedException e) {
                    return "机器人的权限不足，无法执行";
                }
            }
            return "踢出成功！！";
        }else return "您的权限不足，无法执行";
    }

    @Action("查询 {qqNo}")
    @QMsg(at = true, atNewLine = true)
    public String query(Long group, Long qqNo) throws IOException {
        Result<GroupMember> result = qqGroupLogic.queryMemberInfo(botLogic.getQQLoginEntity(), group, qqNo);
        GroupMember groupMember = result.getData();
        if (groupMember == null) return result.getMessage();
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return "群名片：" + groupMember.getGroupCard() + "\n" +
                "Q龄：" + groupMember.getAge() + "\n" +
                "入群时间：" + DateTimeFormatterUtils.format(groupMember.getJoinTime(), pattern) + "\n" +
                "最后发言时间：" + DateTimeFormatterUtils.format(groupMember.getLastTime(), pattern);
    }

    @Action("天气 {local}")
    public Message weather(String local, QQLoginEntity qqLoginEntity, Long qq) throws IOException {
        Result<String> result = toolLogic.weather(local, qqLoginEntity.getCookie());
        if (result.getCode() == 200){
            return FunKt.getMif().xmlEx(146, result.getData()).toMessage();
        }else return FunKt.getMif().at(qq).plus(result.getMessage());
    }

    @Action("龙王")
    public Message dragonKing(Long group, Member qq, @PathVar(value = 1, type = PathVar.Type.Integer) Integer num) throws IOException {
        GroupEntity groupEntity = groupService.findByGroup(group);
        List<Map<String, String>> list = qqGroupLogic.groupHonor(botLogic.getQQLoginEntity(), group, "talkAtIve");
        if (list.size() == 0) return FunKt.getMif().at(qq.getId()).plus("昨天没有龙王！！");
        if (num == null) num = 1;
        if (num > list.size()){
            return FunKt.getMif().at(qq.getId()).plus("历史龙王只有" + list.size() + "位哦，超过范围了！！");
        }
        Map<String, String> map = list.get(num - 1);
        long resultQQ = Long.parseLong(map.get("qq"));
        if (Long.parseLong(this.qq) == resultQQ){
            String[] arr = {"呼风唤雨", "84消毒", "巨星排面"};
            return Message.Companion.toMessage(arr[(int) (Math.random() * arr.length)]);
        }
        if (groupEntity != null) {
            JSONArray whiteJsonArray = groupEntity.getWhiteJsonArray();
            if (whiteJsonArray.contains(String.valueOf(resultQQ))){
                try {
                    qq.ban(60 * 5);
                    return FunKt.getMif().at(qq.getId()).plus("迫害白名单用户，您已被禁言！！");
                }catch (PermissionDeniedException e){
                    return FunKt.getMif().at(qq.getId()).plus("禁止迫害白名单用户，禁言迫害者失败，权限不足！！");
                }
            }
        }
        String[] urlArr = {
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/0bd0d4c6-0ebb-4811-ba06-a0d65c3a8ed3.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/32c1791e-0cb5-4888-a99f-dd8bdd654423.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/493dfe13-bebb-4cd7-8d77-d0bde395db68.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c4cfb6b0-1e67-4f23-9d6e-80a03fb5f91f.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/568877f3-f62b-4cc1-97ee-0d48da8dfb59.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/f39ebff2-03c0-4cee-8967-206562cc055e.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8bbf31d5-878b-4d42-9aa0-a41fd8e13ea6.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c3aa3d94-5cf7-47e1-ba56-db9116b1bcae.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/d13d84e5-e7fa-4d1b-ae6c-1413ffc78769.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/b465c944-8373-4d8c-beda-56eb7c24fa0b.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/b049eb61-dca3-4541-b3dd-c220ccd94595.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/682c4454-fc52-41c3-9c44-890aaa08c03d.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/00d716cf-f691-42ea-aa71-e28f18a3b4b3.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8635cd24-5d87-4fc8-b429-425e02b22849.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/7309fe37-7e34-4b7e-9304-5a1a854d251c.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c631afd3-9614-403c-a5a1-18413bbe3374.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/cfa9129d-e99d-491b-932d-e353ce7ca2d8.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/40960b38-781d-43b0-863b-8962a5342020.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c3e83c57-242a-4843-af51-85a84f7badaf.gif",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8e4d291b-e6ba-48d9-b8f9-3adc56291c27.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8bcad94b-aff5-4e81-af89-8a1007eda4ae.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/dc8403a0-caec-40e0-98a8-93abdb263712.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/1468ee00-a106-42c7-9ce3-0ced6b2ddc3e.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/959cd1ef-8731-4379-b1ad-0d3bf66e38c0.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/79c484e0-695c-49e9-9514-bcbe294ca7c6.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/f9b48126-fb7e-4482-b5ce-140294f57066.png",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/732a6387-2595-4c56-80f8-c52fce6214bb.jpg",
                "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/bb768136-d96d-451d-891a-5f409f7fbff1.jpg"
        };
        String url = urlArr[(int) (Math.random() * urlArr.length)];
        return FunKt.getMif().at(resultQQ).plus(FunKt.getMif().imageByUrl(url)).plus("龙王，已蝉联" + map.get("desc") + "，快喷水！！");
    }

    @Action("群聊炽焰")
    @Synonym({"群聊之火", "冒尖小春笋", "快乐源泉"})
    public Message legend(Long group, Long qq, @PathVar(0) String str, @PathVar(value = 1, type = PathVar.Type.Integer) Integer num) throws IOException {
        String msg;
        List<Map<String, String>> list;
        switch (str){
            case "群聊炽焰":
                list = qqGroupLogic.groupHonor(botLogic.getQQLoginEntity(), group, "legend");
                msg = "快续火！！";
                break;
            case "群聊之火":
                list = qqGroupLogic.groupHonor(botLogic.getQQLoginEntity(), group, "actor");
                msg = "快续火！！";
                break;
            case "冒尖小春笋":
                list = qqGroupLogic.groupHonor(botLogic.getQQLoginEntity(), group, "strongNewBie");
                msg = "快......我也不知道快啥了！！";
                break;
            case "快乐源泉":
                list = qqGroupLogic.groupHonor(botLogic.getQQLoginEntity(), group, "emotion");
                msg = "快发表情包！！";
                break;
            default: return FunKt.getMif().at(qq).plus("类型不匹配，查询失败！！");
        }
        if (list.size() == 0) return FunKt.getMif().at(qq).plus("该群还没有" + str + "用户！！");
        if (num == null) num = 1;
        if (num > list.size()) return FunKt.getMif().at(qq).plus(str + "只有" + list.size() + "位哦，超过范围了！！");
        Map<String, String> map = list.get(num - 1);
        return FunKt.getMif().at(Long.parseLong(map.get("qq"))).plus(FunKt.getMif().imageByUrl(map.get("image"))).plus(str + "，" + map.get("desc") + "，" + msg);
    }

    @Action("群精华")
    public Message essenceMessage(Long group) throws IOException {
        Result<List<JSONArray>> result = qqGroupLogic.essenceMessage(botLogic.getQQLoginEntity(), group);
        List<JSONArray> list = result.getData();
        if (list == null) return BotUtils.toMessage(result.getMessage());
        JSONArray jsonArray = list.get((int) (Math.random() * list.size()));
        return BotUtils.jsonArrayToMessage(jsonArray);
    }
}
