package me.kuku.yuq.controller.manage;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.*;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@GroupController
@SuppressWarnings("unused")
public class ManageSuperAdminController {
    @Config("YuQ.Mirai.bot.master")
    private String master;
    @Inject
    private GroupService groupService;
    @Inject
    private WeiboLogic weiboLogic;
    @Inject
    private BiliBiliLogic biliBiliLogic;

    @Before
    public GroupEntity before(long group, long qq){
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (String.valueOf(qq).equals(master) || groupEntity.isSuperAdmin(qq)) return groupEntity;
        else throw FunKt.getMif().at(qq).plus("您的权限不足，无法执行！！").toThrowable();
    }

    @Action("加违规词 {content}")
    @Synonym({"加黑名单 {content}", "加白名单 {content}", "加拦截 {content}",
            "加微博监控 {content}", "加哔哩哔哩监控 {content}"})
    @QMsg(at = true)
    public String add(GroupEntity groupEntity, @PathVar(0) String type, String content, ContextSession session, long qq) throws IOException {
        switch (type){
            case "加违规词":
                groupEntity.setViolationJsonArray(groupEntity.getViolationJsonArray().fluentAdd(content));
                break;
            case "加黑名单":
                groupEntity.setBlackJsonArray(groupEntity.getBlackJsonArray().fluentAdd(content));
                break;
            case "加白名单":
                groupEntity.setWhiteJsonArray(groupEntity.getWhiteJsonArray().fluentAdd(content));
                break;
            case "加拦截":
                groupEntity.setInterceptJsonArray(groupEntity.getInterceptJsonArray().fluentAdd(content));
                break;
            case "加微博监控":
                Result<List<WeiboPojo>> result = weiboLogic.getIdByName(content);
                if (result.getCode() != 200) return "该用户不存在！！";
                WeiboPojo weiboPojo = result.getData().get(0);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", weiboPojo.getUserId());
                jsonObject.put("name", weiboPojo.getName());
                groupEntity.setWeiboJsonArray(groupEntity.getWeiboJsonArray().fluentAdd(jsonObject));
                break;
            case "加哔哩哔哩监控":
                Result<List<BiliBiliPojo>> blResult = biliBiliLogic.getIdByName(content);
                if (blResult.getCode() != 200) return "该用户不存在";
                BiliBiliPojo biliBiliPojo = blResult.getData().get(0);
                JSONObject blJsonObject = new JSONObject();
                blJsonObject.put("id", biliBiliPojo.getUserId());
                blJsonObject.put("name", biliBiliPojo.getName());
                groupEntity.setBiliBiliJsonArray(groupEntity.getBiliBiliJsonArray().fluentAdd(blJsonObject));
                break;
            default: return null;
        }
        groupService.save(groupEntity);
        return type + "成功";
    }

    @Action("删违规词 {content}")
    @Synonym({"删黑名单 {content}", "删白名单 {content}", "删拦截 {content}",
            "删微博监控 {content}", "删哔哩哔哩监控 {content}"})
    @QMsg(at = true)
    public String del(GroupEntity groupEntity, @PathVar(0) String type, String content){
        switch (type){
            case "删违规词":
                JSONArray violationJsonArray = groupEntity.getViolationJsonArray();
                BotUtils.delManager(violationJsonArray, content);
                groupEntity.setViolationJsonArray(violationJsonArray);
                break;
            case "删黑名单":
                JSONArray blackJsonArray = groupEntity.getBlackJsonArray();
                BotUtils.delManager(blackJsonArray, content);
                groupEntity.setBlackJsonArray(blackJsonArray);
                break;
            case "删白名单":
                JSONArray whiteJsonArray = groupEntity.getWhiteJsonArray();
                BotUtils.delManager(whiteJsonArray, content);
                groupEntity.setWhiteJsonArray(whiteJsonArray);
                break;
            case "删拦截":
                JSONArray interceptJsonArray = groupEntity.getInterceptJsonArray();
                BotUtils.delManager(interceptJsonArray, content);
                groupEntity.setInterceptJsonArray(interceptJsonArray);
                break;
            case "删微博监控":
                JSONArray weiboJsonArray = groupEntity.getWeiboJsonArray();
                BotUtils.delManager(weiboJsonArray, content);
                groupEntity.setWeiboJsonArray(weiboJsonArray);
                break;
            case "删哔哩哔哩监控":
                JSONArray biliBiliJsonArray = groupEntity.getBiliBiliJsonArray();
                BotUtils.delMonitorList(biliBiliJsonArray, content);
                groupEntity.setBiliBiliJsonArray(biliBiliJsonArray);
                break;
            default: return null;
        }
        groupService.save(groupEntity);
        return type + "成功！！";
    }

    @Action("违规次数 {count}")
    @QMsg(at = true)
    public String maxViolationCount(GroupEntity groupEntity, int count){
        groupEntity.setMaxViolationCount(count);
        groupService.save(groupEntity);
        return "已设置本群最大违规次数为" + count + "次";
    }

    @Action("指令限制 {count}")
    @QMsg(at = true)
    public String maxCommandCount(GroupEntity groupEntity, int count){
        groupEntity.setMaxCommandCountOnTime(count);
        groupService.save(groupEntity);
        return "已设置本群单个指令每人每分钟最大触发次数为" + count + "次";
    }

    @Action("色图切换 {type}")
    @QMsg(at = true)
    public String colorPicType(GroupEntity groupEntity, String type){
        String colorPicType;
        if ("lolicon".equals(type) || "loliconR18".equals(type) || type.contains("danbooru")){
            colorPicType = type;
        }else return "没有该类型，请重试！！";
        groupEntity.setColorPicType(colorPicType);
        groupService.save(groupEntity);
        return "色图切换成" + type + "成功！！";
    }

    @Action("加问答 {q}")
    @QMsg(at = true)
    public String qa(ContextSession session, long qq, GroupEntity groupEntity, String q, Group group){
        MessageItemFactory mif = FunKt.getMif();
        group.sendMessage(mif.at(qq).plus("请输入回答语句！！"));
        Message a = session.waitNextMessage();
        JSONObject jsonObject = new JSONObject();
        JSONArray aJsonArray = BotUtils.messageToJsonArray(a);
        jsonObject.put("q", q);
        jsonObject.put("a", aJsonArray);
        jsonObject.put("type", "PARTIAL");
        JSONArray jsonArray = groupEntity.getQaJsonArray();
        jsonArray.add(jsonObject);
        groupEntity.setQaJsonArray(jsonArray);
        groupService.save(groupEntity);
        return "添加问答成功！！";
    }

    @Action("删问答 {q}")
    @QMsg(at = true)
    public String delQa(GroupEntity groupEntity, String q){
        JSONArray qaJsonArray = groupEntity.getQaJsonArray();
        List<JSONObject> delList = new ArrayList<>();
        for (int i = 0; i < qaJsonArray.size(); i++){
            JSONObject jsonObject = qaJsonArray.getJSONObject(i);
            if (q.equals(jsonObject.getString("q"))){
                delList.add(jsonObject);
            }
        }
        delList.forEach(qaJsonArray::remove);
        groupEntity.setQaJsonArray(qaJsonArray);
        groupService.save(groupEntity);
        return "删除问答成功！！";
    }
}
