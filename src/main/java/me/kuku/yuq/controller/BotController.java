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
import me.kuku.yuq.utils.DateTimeFormatterUtils;
import net.mamoe.mirai.contact.PermissionDeniedException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public String tNotSpeakByDay(Group group, long qq){
        if (notSpeakByDay == null) return "请先发送<列出x天未发言>！！";
        GroupEntity groupEntity = groupService.findByGroup(group.getId());
        if (groupEntity.isSuperAdmin(qq) || qq == Long.parseLong(master)){
            for (Long innerQQ : notSpeakByDay) {
                try {
                    group.get(innerQQ).kick("一定时间内未发言");
                } catch (PermissionDeniedException e) {
                    e.printStackTrace();
                    return "机器人的权限不足，无法执行";
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
                    group.get(innerQQ).kick("从未发言");
                } catch (PermissionDeniedException e) {
                    e.printStackTrace();
                    return "机器人的权限不足，无法执行";
                }
            }
            return "踢出成功！！";
        }else return "您的权限不足，无法执行";
    }

    @Action("查询 {qqNo}")
    @QMsg(at = true, atNewLine = true)
    public String query(Long group, Long qqNo){
        Result<GroupMember> result = qqGroupLogic.queryMemberInfo(group, qqNo);
        GroupMember groupMember = result.getData();
        if (groupMember == null) return result.getMessage();
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return "群名片：" + groupMember.getGroupCard() + "\n" +
                "Q龄：" + groupMember.getAge() + "\n" +
                "入群时间：" + DateTimeFormatterUtils.format(groupMember.getJoinTime(), pattern) + "\n" +
                "最后发言时间：" + DateTimeFormatterUtils.format(groupMember.getLastTime(), pattern);
    }

    @QMsg(at = true)
    @Action("群链接")
    public String groupLink(long group, QQLoginEntity qqLoginEntity) throws IOException {
        return qqLoginLogic.getGroupLink(qqLoginEntity, group);
    }

    @Action("天气 {local}")
    public Message weather(String local, QQLoginEntity qqLoginEntity, Long qq) throws IOException {
        Result<String> result = toolLogic.weather(local, qqLoginEntity.getCookie());
        if (result.getCode() == 200){
            return FunKt.getMif().xmlEx(146, result.getData()).toMessage();
        }else return FunKt.getMif().at(qq).plus(result.getMessage());
    }

    @Action("龙王")
    public Message dragonKing(Long group, Member qq, @PathVar(value = 1, type = PathVar.Type.Integer) Integer num){
        GroupEntity groupEntity = groupService.findByGroup(group);
        List<Map<String, String>> list = qqGroupLogic.groupHonor(group, "talkAtIve");
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
                "https://img.kuku.me/images/2021/01/17/RxFy.png",
                "https://img.kuku.me/images/2021/01/17/R0jz.jpg",
                "https://img.kuku.me/images/2021/01/17/Re8w.jpg",
                "https://img.kuku.me/images/2021/01/17/RjnY.jpg",
                "https://img.kuku.me/images/2021/01/17/RPwJ.png",
                "https://img.kuku.me/images/2021/01/17/RsJ9.png",
                "https://img.kuku.me/images/2021/01/17/Rv3t.jpg",
                "https://img.kuku.me/images/2021/01/17/R5ND.jpg",
                "https://img.kuku.me/images/2021/01/17/RNI5.jpg",
                "https://img.kuku.me/images/2021/01/17/Rkor.jpg",
                "https://img.kuku.me/images/2021/01/17/R9P2.gif",
                "https://img.kuku.me/images/2021/01/17/RSFP.jpg",
                "https://img.kuku.me/images/2021/01/17/RaLQ.jpg",
                "https://img.kuku.me/images/2021/01/17/RwnF.jpg",
                "https://img.kuku.me/images/2021/01/17/R7wx.jpg",
                "https://img.kuku.me/images/2021/01/17/REW8.jpg",
                "https://img.kuku.me/images/2021/01/17/Rh3C.png",
                "https://img.kuku.me/images/2021/01/17/RlNq.jpg",
                "https://img.kuku.me/images/2021/01/17/RH6g.jpg",
                "https://img.kuku.me/images/2021/01/17/RKoU.png",
                "https://img.kuku.me/images/2021/01/17/RRPG.jpg",
                "https://img.kuku.me/images/2021/01/17/422Hd.jpg",
                "https://img.kuku.me/images/2021/01/17/42ILN.jpg",
                "https://img.kuku.me/images/2021/01/17/426zV.jpg",
                "https://img.kuku.me/images/2021/01/29/447pC.png",
                "https://img.kuku.me/images/2021/01/29/44C5q.png",
                "https://img.kuku.me/images/2021/01/29/44h4g.png",
                "https://img.kuku.me/images/2021/01/29/44lZU.png"
        };
        String url = urlArr[(int) (Math.random() * urlArr.length)];
        return FunKt.getMif().at(resultQQ).plus(FunKt.getMif().imageByUrl(url)).plus("龙王，已蝉联" + map.get("desc") + "，快喷水！！");
    }

    @Action("群聊炽焰")
    @Synonym({"群聊之火", "冒尖小春笋", "快乐源泉"})
    public Message legend(Long group, Long qq, @PathVar(0) String str, @PathVar(value = 1, type = PathVar.Type.Integer) Integer num){
        String msg;
        List<Map<String, String>> list;
        switch (str){
            case "群聊炽焰":
                list = qqGroupLogic.groupHonor(group, "legend");
                msg = "快续火！！";
                break;
            case "群聊之火":
                list = qqGroupLogic.groupHonor(group, "actor");
                msg = "快续火！！";
                break;
            case "冒尖小春笋":
                list = qqGroupLogic.groupHonor(group, "strongNewBie");
                msg = "快......我也不知道快啥了！！";
                break;
            case "快乐源泉":
                list = qqGroupLogic.groupHonor(group, "emotion");
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
    public String essenceMessage(Long group){
        Result<List<String>> result = qqGroupLogic.essenceMessage(group);
        List<String> list = result.getData();
        if (list == null) return result.getMessage();
        return list.get((int) (Math.random() * list.size()));
    }
}
