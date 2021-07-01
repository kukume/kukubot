package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.*;
import me.kuku.yuq.service.*;
import me.kuku.yuq.utils.*;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    @Action("超级签到")
    @QMsg(at = true, atNewLine = true)
    public String allSign(QQLoginEntity qqLoginEntity, Group group, long qq) throws IOException {
        group.sendMessage(FunKt.getMif().at(qq).plus("请稍后！！！正在为您签到中~~~"));
        try {
            StringBuilder sb = new StringBuilder();
            String str1;
            if (qqLoginLogic.vipSign(qqLoginEntity).contains("失败"))
                str1 = "签到失败";
            else str1 = "签到成功";
            String str2 = qqLoginLogic.yellowSign(qqLoginEntity);
            String str3 = qqLoginLogic.qqVideoSign1(qqLoginEntity);
            String str4 = qqLoginLogic.qqVideoSign2(qqLoginEntity);
            String str5 = qqLoginLogic.bigVipSign(qqLoginEntity);
            String str6;
            if (qqLoginLogic.qqMusicSign(qqLoginEntity).contains("失败"))
                str6 = "签到失败";
            else str6 = "签到成功";
            String str7;
            if (qqLoginLogic.qPetSign(qqLoginEntity).contains("失败"))
                str7 = "领取失败";
            else str7 = "领取成功";
            String str8 = qqLoginLogic.motionSign(qqLoginEntity);
            String str9;
            if (qqLoginLogic.blueSign(qqLoginEntity).contains("成功"))
                str9 = "签到成功";
            else str9 = "签到失败";
            String str10 = qqLoginLogic.sVipMornSign(qqLoginEntity);
            String str11 = qqLoginLogic.weiYunSign(qqLoginEntity);
            String str12 = qqLoginLogic.growthLike(qqLoginEntity);
            sb.append("会员签到：").append(str1).append("\n")
                    .append("黄钻签到：").append(str2).append("\n")
                    .append("腾讯视频签到1：").append(str3).append("\n")
                    .append("腾讯视频签到2：").append(str4).append("\n")
                    .append("大会员签到；").append(str5).append("\n")
                    .append("音乐签到：").append(str6).append("\n")
                    .append("大乐斗签到：").append(str7).append("\n")
                    .append("运动签到：").append(str8).append("\n")
                    .append("蓝钻签到：").append(str9).append("\n")
                    .append("svip打卡报名：").append(str10).append("\n")
                    .append("微云签到：").append(str11).append("\n")
                    .append("排行榜点赞：").append(str12);
            return sb.toString();
//                return "超级签到成功！！";
        }catch (Exception e){
            return "超级签到失败！！异常信息为：" + e.getMessage();
        }
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

    @GroupController
    @PrivateController
    public static class BindQQController {
        @Inject
        private QQLoginService qqLoginService;

        @Action("qqlogin qr")
        @QMsg(at = true)
        public Message bindQQ(Group group, long qq){
            InputStream is = null;
            try {
                Map<String, Object> map = QQQrCodeLoginUtils.getQrCode();
                is = (InputStream) map.get("qrCode");
                ExecutorUtils.execute(() -> {
                    String msg;
                    try {
                        Result<Map<String, String>> result = QQUtils.qrCodeLoginVerify(map.get("sig").toString());
                        if (result.getCode() == 200) {
                            QQUtils.saveOrUpdate(qqLoginService, result.getData(), qq, null, group.getId());
                            msg = "绑定或更新QQ成功！！";
                        } else msg = result.getMessage();
                    } catch (IOException e) {
                        e.printStackTrace();
                        msg = "";
                    }
                    group.sendMessage(FunKt.getMif().at(qq).plus(msg));
                });
                group.sendMessage(FunKt.getMif().at(qq).plus("QQ8.4.8版本以上的不支持直接图片或者相册识别，\n解决方法：用tim或QQhd扫码或使用旧版本QQ（https://wwx.lanzoux.com/igkqMhpj5gh）"));
                return FunKt.getMif().imageByInputStream(is).plus("qzone.qq.com的扫码登录");
            } catch (Exception e) {
                e.printStackTrace();
                return BotUtils.toMessage("出现异常了，请重试。");
            } finally {
                IOUtils.close(is);
            }
        }

        @Action("qqlogin pwd")
        public String bindQQByPwd(@PathVar(2) String password, Contact qq, ContextSession session) throws IOException {
            QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qq.getId());
            Long group = null;
            if (qq instanceof Member) group = ((Member) qq).getGroup().getId();
            String pwd = null;
            if (password != null){
                pwd = password;
            }else if (qqLoginEntity != null) pwd = qqLoginEntity.getPassword();
            if (pwd == null) return "在您的指令中没有发现密码！！";
            Result<Map<String, String>> result = QQPasswordLoginUtils.login(qq.getId(), pwd);
            switch (result.getCode()){
                case 200:
                    Map<String, String> map = result.getData();
                    QQUtils.saveOrUpdate(qqLoginService, map, qq.getId(), pwd, group);
                    return "绑定或者更新QQ成功！！";
                case 10009:
                    qq.sendMessage(Message.Companion.toMessage(result.getMessage()));
                    Map<String, String> smsMap = result.getData();
                    Message codeMessage = session.waitNextMessage(1000 * 60 * 2);
                    String code = Message.Companion.firstString(codeMessage);
                    smsMap.put("smsCode", code);
                    Result<Map<String, String>> loginResult = QQPasswordLoginUtils.loginBySms(qq.getId(), password, smsMap);
                    if (loginResult.getCode() != 200) return "验证码输入错误，请重新登录！！";
                    QQUtils.saveOrUpdate(qqLoginService, loginResult.getData(), qq.getId(), pwd, group);
                    return "绑定或者更新QQ成功！！";
                default: return result.getMessage();

            }
        }
    }

    @GroupController
    public static class QQJobController {
        @Inject
        private QQJobService qqJobService;
        @Inject
        private QQLoginService qqLoginService;

        @Before
        public void check(long qq, BotActionContext actionContext){
            QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qq);
            if (qqLoginEntity == null) throw FunKt.getMif().at(qq).plus("没有绑定QQ！！").toThrowable();
            else actionContext.set("qqLoginEntity", qqLoginEntity);
        }

        @QMsg(at = true)
        @Action("秒赞 {status}")
        public String mzOpen(long qq, boolean status){
            QQJobEntity qqJobEntity = qqJobService.findByQQAndType(qq, "mz");
            if (qqJobEntity == null){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", false);
                qqJobEntity = new QQJobEntity(null, qq, "mz", jsonObject.toString());
            }
            JSONObject jsonObject = qqJobEntity.getDataJsonObject();
            jsonObject.put("status", status);
            qqJobEntity.setDataJsonObject(jsonObject);
            qqJobService.save(qqJobEntity);
            String s = "关闭";
            if (status) s = "开启";
            return "秒赞已" + s;
        }

        @QMsg(at = true)
        @Action("百变气泡/{text}")
        public String varietyBubble(long qq, String text){
            QQJobEntity qqJobEntity = qqJobService.findByQQAndType(qq, "bubble");
            if (qqJobEntity == null){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", false);
                jsonObject.put("text", "");
                qqJobEntity = new QQJobEntity(null, qq, "bubble", jsonObject.toString());
            }
            JSONObject jsonObject = qqJobEntity.getDataJsonObject();
            String msg;
            if ("关".equals(text)){
                jsonObject.put("status", false);
                msg = "百变气泡已关闭！！";
            }else {
                jsonObject.put("status", true);
                jsonObject.put("text", text);
                msg = "百变气泡已开启！！气泡diy文字为：" + text;
            }
            qqJobEntity.setDataJsonObject(jsonObject);
            qqJobService.save(qqJobEntity);
            return msg;
        }

        @QMsg(at = true)
        @Action("自动签到 {status}")
        public String autoSign(long qq, boolean status){
            QQJobEntity qqJobEntity = qqJobService.findByQQAndType(qq, "autoSign");
            if (qqJobEntity == null){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", false);
                qqJobEntity = new QQJobEntity(null, qq, "autoSign", jsonObject.toString());
            }
            JSONObject jsonObject = qqJobEntity.getDataJsonObject();
            jsonObject.put("status", status);
            qqJobEntity.setDataJsonObject(jsonObject);
            qqJobService.save(qqJobEntity);
            String ss = "关闭";
            if (status) ss = "开启";
            return "qq自动签到" + ss + "成功";
        }

        @QMsg(at = true)
        @Action("加说说转发 {qqNo} {content}")
        public String autoForward(long qq, long qqNo, String content){
            QQJobEntity qqJobEntity = qqJobService.findByQQAndType(qq, "autoForward");
            if (qqJobEntity == null){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content", new JSONArray());
                qqJobEntity = new QQJobEntity(null, qq, "autoForward", jsonObject.toString());
            }
            JSONObject dataJsonObject = qqJobEntity.getDataJsonObject();
            JSONArray jsonArray = dataJsonObject.getJSONArray("content");
            JSONObject addJsonObject = new JSONObject();
            addJsonObject.put("qq", qqNo);
            addJsonObject.put("content", content);
            jsonArray.add(addJsonObject);
            dataJsonObject.put("content", jsonArray);
            qqJobEntity.setDataJsonObject(dataJsonObject);
            qqJobService.save(qqJobEntity);
            return "添加说说自动转发成功！！";
        }

        @QMsg(at = true, atNewLine = true)
        @Action("删说说转发 {qqNo}")
        public String delAutoForward(long qq, long qqNo){
            QQJobEntity qqJobEntity = qqJobService.findByQQAndType(qq, "autoForward");
            if (qqJobEntity == null) return "您还没有添加过说说自动转发";
            JSONObject dataJsonObject = qqJobEntity.getDataJsonObject();
            JSONArray jsonArray = dataJsonObject.getJSONArray("content");
            List<JSONObject> delList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getLong("qq") == qqNo) delList.add(jsonObject);
            }
            delList.forEach(jsonArray::remove);
            dataJsonObject.put("content", jsonArray);
            qqJobEntity.setDataJsonObject(dataJsonObject);
            qqJobService.save(qqJobEntity);
            return "删除说说自动转发成功！！";
        }

        @QMsg(at = true)
        @Action("查说说转发")
        public String queryAutoForward(long qq){
            QQJobEntity qqJobEntity = qqJobService.findByQQAndType(qq, "autoForward");
            if (qqJobEntity == null) return "您还没有说说自动转发";
            JSONArray jsonArray = qqJobEntity.getDataJsonObject().getJSONArray("content");
            StringBuilder sb = new StringBuilder().append("您的说说自动转发列表如下").append("\n");
            for (int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                sb.append(jsonObject.getLong("qq")).append("-").append(jsonObject.getString("content"))
                        .append("\n");
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }

}
