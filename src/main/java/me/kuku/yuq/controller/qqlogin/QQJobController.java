package me.kuku.yuq.controller.qqlogin;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import me.kuku.yuq.entity.QQJobEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.service.QQJobService;
import me.kuku.yuq.service.QQLoginService;

import javax.inject.Inject;

@GroupController
public class QQJobController {
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
}
