package me.kuku.yuq.controller.manage;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.RecallEntity;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.RecallService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@GroupController
@SuppressWarnings("unused")
public class ManageNotController {
    @Inject
    private GroupService groupService;
    @Inject
    private RecallService recallService;
    @Inject
    private ToolLogic toolLogic;
    @Config("YuQ.Mirai.bot.version")
    private String version;

    @Before
    public GroupEntity before(Long group){
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) groupEntity = new GroupEntity(group);
        return groupEntity;
    }

    @Action("查管")
    @Synonym({"查黑名单", "查白名单", "查违规词", "查拦截", "查微博监控", "查哔哩哔哩监控", "查问答", "查超管", "查指令限制", "查shell"})
    @QMsg(at = true, atNewLine = true)
    public String query(GroupEntity groupEntity, @PathVar(0) String type){
        StringBuilder sb = new StringBuilder();
        switch (type){
            case "查管":
                sb.append("本群管理员列表如下：").append("\n");
                groupEntity.getAdminJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                break;
            case "查超管":
                sb.append("本群超级管理员列表如下").append("\n");
                groupEntity.getSuperAdminJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                break;
            case "查黑名单":
                sb.append("本群黑名单列表如下：").append("\n");
                groupEntity.getBlackJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                break;
            case "查白名单":
                sb.append("本群白名单列表如下：").append("\n");
                groupEntity.getWhiteJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                break;
            case "查违规词":
                sb.append("本群违规词列表如下：").append("\n");
                groupEntity.getViolationJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                break;
            case "查拦截":
                sb.append("本群被拦截的指令列表如下：").append("\n");
                groupEntity.getInterceptJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                break;
            case "查微博监控":
                sb.append("本群微博监控列表如下：").append("\n");
                groupEntity.getWeiboJsonArray().forEach( obj -> {
                    JSONObject weiboJsonObject = (JSONObject) obj;
                    sb.append(weiboJsonObject.getString("id")).append("-")
                            .append(weiboJsonObject.getString("name")).append("\n");
                });
                break;
            case "查哔哩哔哩监控":
                sb.append("本群哔哩哔哩监控列表如下：").append("\n");
                groupEntity.getBiliBiliJsonArray().forEach( obj -> {
                    JSONObject biliBiliJsonObject = (JSONObject) obj;
                    sb.append(biliBiliJsonObject.getString("id")).append("-")
                            .append(biliBiliJsonObject.getString("name")).append("\n");
                });
                break;
            case "查问答":
                sb.append("本群问答列表如下：").append("\n");
                groupEntity.getQaJsonArray().forEach(obj -> {
                    JSONObject jsonObject = (JSONObject) obj;
                    sb.append(jsonObject.getString("q")).append("\n");
                });
                break;
            case "查指令限制":
                sb.append("本群的指令限制列表如下：").append("\n");
                groupEntity.getCommandLimitJsonObject().forEach((k, v) -> {
                    sb.append(k).append("->").append(v).append("次").append("\n");
                });
                break;
            case "查shell":
                sb.append("本群的shell命令存储如下").append("\n");
                groupEntity.getShellCommandJsonArray().forEach(obj -> {
                    JSONObject shellCommandJsonObject = (JSONObject) obj;
                    sb.append(shellCommandJsonObject.getInteger("auth")).append("->")
                            .append(shellCommandJsonObject.getString("command")).append("->")
                            .append(shellCommandJsonObject.getString("shell"));
                });
                break;
            default: return null;
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Action("查撤回 {qqNo}")
    public Message queryRecall(long group, long qqNo, long qq, @PathVar(value = 2, type = PathVar.Type.Integer) Integer num){
        List<RecallEntity> recallList = recallService.findByGroupAndQQ(group, qqNo);
        int all = recallList.size();
        if (num == null) num = 1;
        if (num > all || num < 0) return FunKt.getMif().at(qq).plus("您要查询的QQ只有" + all + "条撤回消息，超过范围了！！");
        RecallEntity recallEntity = recallList.get(num - 1);
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recallEntity.getDate());
        return FunKt.getMif().at(qq).plus("\n该消息撤回时间为" + timeStr + "\n消息内容为：\n")
                .plus(BotUtils.jsonArrayToMessage(recallEntity.getMessageEntity().getContentJsonArray()));
    }

    @Action("检查版本")
    public String checkUpdate() throws IOException {
        String gitVersion = toolLogic.queryVersion();
        return "当前程序版本：" + this.version + "\n" +
                "最新程序版本：" + gitVersion;
    }

    @Action("开关")
    @QMsg(at = true, atNewLine = true)
    public String kai(GroupEntity groupEntity){
        StringBuilder sb = new StringBuilder("本群开关情况如下：\n");
        sb.append("色图：").append(this.boolToStr(groupEntity.getColorPic())).append("、").append(groupEntity.getColorPicType()).append("\n");
        sb.append("鉴黄：").append(this.boolToStr(groupEntity.getPic())).append("\n");
        sb.append("欢迎语：").append(this.boolToStr(groupEntity.getWelcomeMsg())).append("\n");
        sb.append("退群拉黑：").append(this.boolToStr(groupEntity.getLeaveGroupBlack())).append("\n");
        sb.append("自动审核：").append(this.boolToStr(groupEntity.getAutoReview())).append("\n");
        sb.append("撤回通知：").append(this.boolToStr(groupEntity.getRecall())).append("\n");
        sb.append("整点报时：").append(this.boolToStr(groupEntity.getOnTimeAlarm())).append("\n");
        sb.append("闪照通知：").append(this.boolToStr(groupEntity.getFlashNotify())).append("\n");
        Integer maxCommandCountOnTime = groupEntity.getMaxCommandCountOnTime();
        if (maxCommandCountOnTime == null) maxCommandCountOnTime = -1;
        String ss = maxCommandCountOnTime.toString();
        if (maxCommandCountOnTime < 0) ss = "无限制";
        sb.append("指令限制：").append(ss).append("\n");
        Integer maxViolationCount = groupEntity.getMaxViolationCount();
        if (maxViolationCount == null) maxViolationCount = 5;
        sb.append("最大违规次数：").append(maxViolationCount);
        return sb.toString();
    }

    private String boolToStr(Boolean b){
        if (b == null || !b) return "关";
        else return "开";
    }
}
