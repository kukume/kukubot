package me.kuku.yuq.controller.qqlogin;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Image;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItem;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQLoginLogic;
import me.kuku.yuq.logic.QQMailLogic;
import me.kuku.yuq.logic.QQZoneLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.QQLoginService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@GroupController
public class QQLoginController extends QQController {
    @Inject
    private QQMailLogic qqMailLogic;
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

    @Action("气泡")
    @QMsg(at = true)
    public String bubble(@PathVar(1) String text, @PathVar(2) String name, QQLoginEntity qqLoginEntity) throws IOException {
        if (text != null) return qqLoginLogic.diyBubble(qqLoginEntity, text, name);
        else return "缺少参数：diy气泡文本内容！";
    }

    @Action("业务")
    @QMsg(at = true, atNewLine = true)
    public String queryVip(QQLoginEntity qqLoginEntity) throws IOException {
        return qqLoginLogic.queryVip(qqLoginEntity);
    }

    @Action("昵称")
    @QMsg(at = true)
    public String modifyNickname(@PathVar(1) String str, QQLoginEntity qqLoginEntity) throws IOException {
        if (str != null) return qqLoginLogic.modifyNickname(qqLoginEntity, str);
        else return qqLoginLogic.modifyNickname(qqLoginEntity, " ");
    }

    @Action("头像")
    @QMsg(at = true)
    public String modifyAvatar(QQLoginEntity qqLoginEntity, Message message) throws IOException {
        String url;
        try {
            MessageItem singleBody = message.getBody().get(1);
            if (singleBody instanceof Image){
                url = ((Image) singleBody).getUrl();
            }else return "请携带一张头像";
        }catch (IndexOutOfBoundsException e){
            url = "http://qqpublic.qpic.cn/qq_public/0/0-3083588061-157B50D7A4036953784514241D7DDC19/0";
        }
        return qqLoginLogic.modifyAvatar(qqLoginEntity, url);
    }

    @Action("送花 {qqNo}")
    @QMsg(at = true)
    public String sendFlower(QQLoginEntity qqLoginEntity, Long qqNo, long group) throws IOException {
        return qqLoginLogic.sendFlower(qqLoginEntity, qqNo, group);
    }

    @Action("群礼物")
    @QMsg(at = true)
    public String lottery(QQLoginEntity qqLoginEntity, Group group, Long qq) throws IOException {
        Result<List<Map<String, String>>> result = qqZoneLogic.queryGroup(qqLoginEntity);
        if (result.getCode() == 200){
            new Thread(() -> {
                List<Map<String, String>> list = result.getData();
                StringBuilder sb = new StringBuilder();
                if (list != null){
                    list.forEach(map -> {
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        long itGroup = Long.parseLong(map.get("group"));
                        try {
                            String loResult = qqLoginLogic.groupLottery(qqLoginEntity, itGroup);
                            if (loResult.contains("成功")) sb.append(loResult).append("\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    String str;
                    if (sb.toString().equals("")) str = "啥都没抽到";
                    else str = sb.deleteCharAt(sb.length() - 1).toString();
                    group.sendMessage(FunKt.getMif().at(qq).plus(str));
                }
            }).start();
            return "抽礼物正在运行中";
        }else return result.getMessage();
    }

    @Action("超级签到")
    @QMsg(at = true, atNewLine = true)
    public String allSign(QQLoginEntity qqLoginEntity, long group, long qq) throws IOException {
        reply(FunKt.getMif().at(qq).plus("请稍后！！！正在为您签到中~~~"));
        String str1 = qqLoginLogic.qqSign(qqLoginEntity);
        if (!str1.contains("更新QQ")){
            try {
                StringBuilder sb = new StringBuilder();
                qqLoginLogic.anotherSign(qqLoginEntity);
                String str2 = qqLoginLogic.groupLottery(qqLoginEntity, group);
                String str3;
                if (qqLoginLogic.vipSign(qqLoginEntity).contains("失败"))
                    str3 = "签到失败";
                else str3 = "签到成功";
                String str4 = qqLoginLogic.phoneGameSign(qqLoginEntity);
                String str5 = qqLoginLogic.yellowSign(qqLoginEntity);
                String str6 = qqLoginLogic.qqVideoSign1(qqLoginEntity);
                String str7 = qqLoginLogic.qqVideoSign2(qqLoginEntity);
                String str8 = qqLoginLogic.bigVipSign(qqLoginEntity);
                String str9;
                if (qqLoginLogic.qqMusicSign(qqLoginEntity).contains("失败"))
                    str9 = "签到失败";
                else str9 = "签到成功";
                String str10;
                if (qqLoginLogic.qPetSign(qqLoginEntity).contains("失败"))
                    str10 = "领取失败";
                else str10 = "领取成功";
                String str11;
                if (qqLoginLogic.tribeSign(qqLoginEntity).contains("成功"))
                    str11 = "领取成功";
                else str11 = "领取失败";
                String str12 = qqLoginLogic.motionSign(qqLoginEntity);
                String str13;
                if (qqLoginLogic.blueSign(qqLoginEntity).contains("成功"))
                    str13 = "签到成功";
                else str13 = "签到失败";
                String str14 = qqLoginLogic.sVipMornSign(qqLoginEntity);
                String str15 = qqLoginLogic.weiYunSign(qqLoginEntity);
                String str16 = qqLoginLogic.weiShiSign(qqLoginEntity);
                String str17 = qqLoginLogic.growthLike(qqLoginEntity);
                sb.append("手机打卡：").append(str1).append("\n")
                        .append("群等级抽奖：").append(str2).append("\n")
                        .append("会员签到：").append(str3).append("\n")
                        .append("手游加速：").append(str4).append("\n")
                        .append("黄钻签到：").append(str5).append("\n")
                        .append("腾讯视频签到1：").append(str6).append("\n")
                        .append("腾讯视频签到2：").append(str7).append("\n")
                        .append("大会员签到；").append(str8).append("\n")
                        .append("音乐签到：").append(str9).append("\n")
                        .append("大乐斗签到：").append(str10).append("\n")
                        .append("兴趣部落：").append(str11).append("\n")
                        .append("运动签到：").append(str12).append("\n")
                        .append("蓝钻签到：").append(str13).append("\n")
                        .append("svip打卡报名：").append(str14).append("\n")
                        .append("微云签到：").append(str15).append("\n")
                        .append("微视签到：").append(str16).append("\n")
                        .append("排行榜点赞：").append(str17);
//                return sb.toString();
                return "超级签到成功！！";
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

    @Action("续期")
    @QMsg(at = true)
    public String renew(QQLoginEntity qqLoginEntity, long qq) throws IOException {
        if (qqLoginEntity.getPassword() == null) return "续期QQ邮箱中转站文件失败！！，需要使用密码登录QQ！";
        reply(FunKt.getMif().at(qq).plus("正在续期中，请稍后~~~~~"));
        return qqMailLogic.fileRenew(qqLoginEntity);
    }

    @Action("复制头像 {qqNo}")
    @QMsg(at = true)
    public String copyAvatar(String qqNo, QQLoginEntity qqLoginEntity) throws IOException {
        String url = "https://q.qlogo.cn/g?b=qq&nk=" + qqNo + "&s=640";
        return qqLoginLogic.modifyAvatar(qqLoginEntity, url);
    }

    @Action("删除qq")
    @QMsg(at = true)
    public String delQQ(QQLoginEntity qqLoginEntity){
        qqLoginService.delByQQ(qqLoginEntity.getQq());
        return "删除QQ成功！！！";
    }

    @Action("自定义机型 {iMei}")
    @QMsg(at = true)
    public String changePhoneOnline(QQLoginEntity qqLoginEntity, String iMei, long qq, ContextSession session) throws IOException {
        reply(FunKt.getMif().at(qq).plus("请输入您需要自定义的机型！！"));
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
