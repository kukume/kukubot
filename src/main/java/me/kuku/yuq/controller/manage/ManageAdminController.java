package me.kuku.yuq.controller.manage;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Member;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.service.GroupService;

import javax.inject.Inject;

@GroupController
@SuppressWarnings("unused")
public class ManageAdminController {
    @Config("YuQ.Mirai.bot.master")
    private String master;
    @Inject
    private GroupService groupService;

    @Before
    public GroupEntity before(Member qq, long group){
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) groupEntity = new GroupEntity(group);
        if (groupEntity.isAdmin(qq.getId()) || qq.getId() == Long.parseLong(master) || qq.isAdmin()){
            return groupEntity;
        }else throw FunKt.getMif().at(qq).plus("您的权限不足，无法执行！！").toThrowable();
    }

    @Action("清屏")
    public String clear(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append("\n");
        return sb.toString();
    }

    @Action("禁言 {qqNo}")
    @QMsg(at = true)
    public String shutUp(long group, long qqNo, @PathVar(2) String timeStr){
        int time;
        if (timeStr == null) time = 0;
        else {
            if (timeStr.length() == 1) return "未发现时间单位！！单位可为（s,m,h,d）";
            int num = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
            switch (timeStr.charAt(timeStr.length() - 1)){
                case 's': time = num; break;
                case 'm': time = num * 60; break;
                case 'h': time = num * 60 * 60; break;
                case 'd': time = num * 60 * 60 * 24; break;
                default: return "禁言时间格式不正确";
            }
        }
        FunKt.getYuq().getGroups().get(group).get(qqNo).ban(time);
        return "禁言成功！！";
    }

    @Action("kukubot {status}")
    @Synonym({"loc监控 {status}", "整点报时 {status}", "自动审核 {status}",
            "欢迎语 {status}", "退群拉黑 {status}", "鉴黄 {status}", "色图 {status}",
            "撤回通知 {status}", "闪照通知 {status}", "复读 {status}", "语音识别 {status}",
            "上传通知 {status}"})
    @QMsg(at = true)
    public String onOrOff(GroupEntity groupEntity, boolean status, @PathVar(0) String op){
        switch (op){
            case "kukubot": groupEntity.setStatus(status); break;
            case "loc监控": groupEntity.setLocMonitor(status); break;
            case "整点报时": groupEntity.setOnTimeAlarm(status); break;
            case "自动审核": groupEntity.setAutoReview(status); break;
            case "欢迎语": groupEntity.setWelcomeMsg(status); break;
            case "退群拉黑": groupEntity.setLeaveGroupBlack(status); break;
            case "鉴黄": groupEntity.setPic(status); break;
            case "色图": groupEntity.setColorPic(status); break;
            case "撤回通知": groupEntity.setRecall(status); break;
            case "闪照通知": groupEntity.setFlashNotify(status); break;
            case "复读": groupEntity.setRepeat(status); break;
            case "语音识别": groupEntity.setVoiceIdentify(status); break;
            case "上传通知": groupEntity.setUploadPicNotice(status); break;
            default: return null;
        }
        groupService.save(groupEntity);
        if (status) return op + "开启成功";
        else return op + "关闭成功";
    }
}
