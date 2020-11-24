package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.QQEntity;
import me.kuku.yuq.logic.MyApiLogic;
import me.kuku.yuq.pojo.InstagramPojo;
import me.kuku.yuq.pojo.TwitterPojo;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.QQService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@GroupController
public class MyQQController extends QQController {

    @Inject
    private QQService qqService;
    @Inject
    private GroupService groupService;
    @Inject
    private MyApiLogic myApiLogic;

    @Before
    public void before(long qq, long group, BotActionContext actionContext){
        QQEntity qqEntity = qqService.findByQQAndGroup(qq, group);
        if (qqEntity == null) {
            GroupEntity groupEntity = groupService.findByGroup(group);
            if (groupEntity == null) groupEntity = new GroupEntity(group);
            qqEntity = new QQEntity(qq, groupEntity);
        }
        actionContext.set("qqEntity", qqEntity );
    }

    @Action("查询违规")
    @QMsg(at = true)
    public String queryVio(long qq, long group){
        QQEntity qqEntity = qqService.findByQQAndGroup(qq, group);
        int num;
        if (qqEntity == null || qqEntity.getViolationCount() == null) num = 0;
        else num = qqEntity.getViolationCount();
        return "您在本群违规次数为" + num + "次";
    }

    @Action("加推特监控 {content}")
    @Synonym({"加ins监控 {content}"})
    @QMsg(at = true)
    public String add(QQEntity qqEntity, @PathVar(0) String type, String content, ContextSession session, long qq) throws IOException {
        switch (type){
            case "加推特监控":
                List<TwitterPojo> twIdList = myApiLogic.findTwitterIdByName(content);
                if (twIdList == null) return "没有找到该用户，请重试";
                StringBuilder idMsg = new StringBuilder();
                for (int i = 0; i < twIdList.size(); i++){
                    TwitterPojo twitterPojo = twIdList.get(i);
                    idMsg.append(i + 1).append("、").append(twitterPojo.getUserId()).append("-").append(twitterPojo.getName()).append("-").append(twitterPojo.getScreenName()).append("\n");
                }
                reply(FunKt.getMif().at(qq).plus("请选择您需要监控的用户，输入序号\n").plus(
                        idMsg.deleteCharAt(idMsg.length() - 1).toString()
                ));
                Message twIdMessage = session.waitNextMessage();
                String numStr = Message.Companion.firstString(twIdMessage);
                int num = Integer.parseInt(numStr);
                if (num > twIdList.size()) return "你所选择的序号超过限制了！！";
                TwitterPojo twitterPojo = twIdList.get(num - 1);
                JSONObject twJsonObject = new JSONObject();
                twJsonObject.put("id", twitterPojo.getUserId());
                twJsonObject.put("name", twitterPojo.getName());
                twJsonObject.put("screenName", twitterPojo.getScreenName());
                qqEntity.setTwitterJsonArray(qqEntity.getTwitterJsonArray().fluentAdd(twJsonObject));
                break;
            case "加ins监控":
                List<InstagramPojo> list = myApiLogic.findInsIdByName(content);
                if (list == null) return "没有找到该用户，请重试！！";
                JSONObject insJsonObject = new JSONObject();
                InstagramPojo instagramPojo = list.get(0);
                insJsonObject.put("id", instagramPojo.getUserId());
                insJsonObject.put("name", instagramPojo.getName());
                insJsonObject.put("fullName", instagramPojo.getFullName());
                qqEntity.setInstagramJsonArray(qqEntity.getInstagramJsonArray().fluentAdd(insJsonObject));
                break;
            default: return null;
        }
        qqService.save(qqEntity);
        return type + "成功！！";
    }

    @Action("删推特监控 {content}")
    @Synonym({"删ins监控 {content}"})
    public String del(QQEntity qqEntity, @PathVar(0) String type, String content){
        switch (type){
            case "删推特监控":
                JSONArray twitterJsonArray = qqEntity.getTwitterJsonArray();
                BotUtils.delMonitorList(twitterJsonArray, content);
                qqEntity.setTwitterJsonArray(twitterJsonArray);
                break;
            case "删ins监控":
                JSONArray instagramJsonArray = qqEntity.getInstagramJsonArray();
                BotUtils.delMonitorList(instagramJsonArray, content);
                qqEntity.setInstagramJsonArray(instagramJsonArray);
                break;
            default: return null;
        }
        qqService.save(qqEntity);
        return type + "成功！！";
    }

    @Action("查推特监控")
    @Synonym({"查ins监控"})
    @QMsg(at = true)
    public String query(QQEntity qqEntity, @PathVar(0) String type){
        StringBuilder sb = new StringBuilder();
        switch (type){
            case "查推特监控":
                sb.append("您的推特监控列表如下：").append("\n");
                qqEntity.getTwitterJsonArray().forEach(obj -> {
                    JSONObject jsonObject = (JSONObject) obj;
                    sb.append(jsonObject.getString("id")).append("-")
                            .append(jsonObject.getString("name")).append("-")
                            .append(jsonObject.getString("screenName")).append("\n");
                });
                break;
            case "查ins监控":
                sb.append("您的ins监控列表如下：").append("\n");
                qqEntity.getInstagramJsonArray().forEach(obj -> {
                    JSONObject jsonObject = (JSONObject) obj;
                    sb.append(jsonObject.getString("id")).append("-")
                            .append(jsonObject.getString("name")).append("-")
                            .append(jsonObject.getString("fullName")).append("\n");
                });
                break;
            default: return null;
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
