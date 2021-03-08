package me.kuku.yuq.controller.qqlogin;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQLoginLogic;
import me.kuku.yuq.logic.QQZoneLogic;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.QQLoginService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@GroupController
public class QQLoginController{
    @Inject
    private QQLoginLogic qqLoginLogic;
    @Inject
    private QQZoneLogic qqZoneLogic;
    @Inject
    private QQLoginService qqLoginService;
    @Inject
    private GroupService groupService;

    @Before
    public void checkBind(long qq, BotActionContext actionContext){
        QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qq);
        if (qqLoginEntity == null)
            throw FunKt.getMif().at(qq).plus("没有绑定QQ！，请先发送<qqlogin qr>进行扫码登录绑定，如需密码登录绑定请私聊机器人发送qq").toThrowable();
        if (qqLoginEntity.getStatus()) actionContext.set("qqLoginEntity", qqLoginEntity);
        else throw FunKt.getMif().at(qq).plus("您的QQ已失效，请更新QQ！！").toThrowable();
    }

    @Action("气泡 {text}")
    @QMsg(at = true)
    public String bubble(String text, @PathVar(2) String name, QQLoginEntity qqLoginEntity) throws IOException {
        return qqLoginLogic.diyBubble(qqLoginEntity, text, name);
    }

    @Action("业务")
    @QMsg(at = true, atNewLine = true)
    public String queryVip(QQLoginEntity qqLoginEntity) throws IOException {
        return qqLoginLogic.queryVip(qqLoginEntity);
    }

    @Action("送花 {qqNo}")
    @QMsg(at = true)
    public String sendFlower(QQLoginEntity qqLoginEntity, Long qqNo, long group) throws IOException {
        return qqLoginLogic.sendFlower(qqLoginEntity, qqNo, group);
    }

    @Action("超级签到")
    @QMsg(at = true, atNewLine = true)
    public String allSign(QQLoginEntity qqLoginEntity, Group group, long qq) throws IOException {
        group.sendMessage(FunKt.getMif().at(qq).plus("请稍后！！！正在为您签到中~~~"));
        String str1 = qqLoginLogic.qqSign(qqLoginEntity);
        if (!str1.contains("更新QQ")){
            try {
                StringBuilder sb = new StringBuilder();
                qqLoginLogic.anotherSign(qqLoginEntity);
                String str2;
                if (qqLoginLogic.vipSign(qqLoginEntity).contains("失败"))
                    str2 = "签到失败";
                else str2 = "签到成功";
                String str3 = qqLoginLogic.yellowSign(qqLoginEntity);
                String str4 = qqLoginLogic.qqVideoSign1(qqLoginEntity);
                String str5 = qqLoginLogic.qqVideoSign2(qqLoginEntity);
                String str6 = qqLoginLogic.bigVipSign(qqLoginEntity);
                String str7;
                if (qqLoginLogic.qqMusicSign(qqLoginEntity).contains("失败"))
                    str7 = "签到失败";
                else str7 = "签到成功";
                String str8;
                if (qqLoginLogic.qPetSign(qqLoginEntity).contains("失败"))
                    str8 = "领取失败";
                else str8 = "领取成功";
                String str9;
                if (qqLoginLogic.tribeSign(qqLoginEntity).contains("成功"))
                    str9 = "领取成功";
                else str9 = "领取失败";
                String str10 = qqLoginLogic.motionSign(qqLoginEntity);
                String str11;
                if (qqLoginLogic.blueSign(qqLoginEntity).contains("成功"))
                    str11 = "签到成功";
                else str11 = "签到失败";
                String str12 = qqLoginLogic.sVipMornSign(qqLoginEntity);
                String str13 = qqLoginLogic.weiYunSign(qqLoginEntity);
                String str14 = qqLoginLogic.growthLike(qqLoginEntity);
                sb.append("手机打卡：").append(str1).append("\n")
                        .append("会员签到：").append(str2).append("\n")
                        .append("黄钻签到：").append(str3).append("\n")
                        .append("腾讯视频签到1：").append(str4).append("\n")
                        .append("腾讯视频签到2：").append(str5).append("\n")
                        .append("大会员签到；").append(str6).append("\n")
                        .append("音乐签到：").append(str7).append("\n")
                        .append("大乐斗签到：").append(str8).append("\n")
                        .append("兴趣部落：").append(str9).append("\n")
                        .append("运动签到：").append(str10).append("\n")
                        .append("蓝钻签到：").append(str11).append("\n")
                        .append("svip打卡报名：").append(str12).append("\n")
                        .append("微云签到：").append(str13).append("\n")
                        .append("排行榜点赞：").append(str14);
                return sb.toString();
//                return "超级签到成功！！";
            }catch (Exception e){
                return "超级签到失败！！";
            }
        }else return "超级签到失败，请更新QQ！！";
    }

    @Action("赞说说")
    @QMsg(at = true)
    public String likeTalk(QQLoginEntity qqLoginEntity) throws IOException {
        List<Map<String, String>> friendTalk = qqZoneLogic.friendTalk(qqLoginEntity);
        if (friendTalk != null){
            friendTalk.forEach(map -> {
                if (map.get("like") == null || !"1".equals(map.get("like"))){
                    try {
                        qqZoneLogic.likeTalk(qqLoginEntity, map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return "赞说说成功！！";
        }else return "赞说说失败，请更新QQ！！";
    }

    @Action("成长")
    @QMsg(at = true, atNewLine = true)
    public String growth(QQLoginEntity qqLoginEntity) throws IOException {
        return qqLoginLogic.vipGrowthAdd(qqLoginEntity);
    }

    @Action("删除qq")
    @QMsg(at = true)
    public String delQQ(QQLoginEntity qqLoginEntity){
        qqLoginService.delByQQ(qqLoginEntity.getQq());
        return "删除QQ成功！！！";
    }

    @Action("自定义机型 {iMei}")
    @QMsg(at = true)
    public String changePhoneOnline(QQLoginEntity qqLoginEntity, String iMei, long qq, ContextSession session, Group group) throws IOException {
        group.sendMessage(FunKt.getMif().at(qq).plus("请输入您需要自定义的机型！！"));
        Message nextMessage = session.waitNextMessage();
        String phone = Message.Companion.firstString(nextMessage);
        return qqLoginLogic.changePhoneOnline(qqLoginEntity, iMei, phone);
    }

    @Action("访问空间 {qqNo}")
    @QMsg(at = true)
    public String visit(long qqNo, QQLoginEntity qqLoginEntity) throws IOException {
        return qqZoneLogic.visitQZone(qqLoginEntity, qqNo);
    }

    @Action("互访")
    @QMsg(at = true)
    public String visitAll(long qq){
        List<QQLoginEntity> list = qqLoginService.findByActivity();
        list.forEach(en -> {
            try {
                qqZoneLogic.visitQZone(en, qq);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return "互访成功！！";
    }
}
