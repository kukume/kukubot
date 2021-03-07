package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.entity.Friend;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.controller.arknights.ArkNightsController;
import me.kuku.yuq.controller.bilibili.BiliBiliController;
import me.kuku.yuq.controller.bilibili.BiliBiliLoginController;
import me.kuku.yuq.controller.manage.ManageAdminController;
import me.kuku.yuq.controller.manage.ManageNotController;
import me.kuku.yuq.controller.manage.ManageSuperAdminController;
import me.kuku.yuq.controller.netease.BindNeTeaseController;
import me.kuku.yuq.controller.netease.NeTeaseController;
import me.kuku.yuq.controller.qqlogin.BindQQController;
import me.kuku.yuq.controller.qqlogin.QQJobController;
import me.kuku.yuq.controller.qqlogin.QQLoginController;
import me.kuku.yuq.controller.qqlogin.QQQuickLoginController;
import me.kuku.yuq.controller.warframe.WarframeController;
import me.kuku.yuq.controller.weibo.WeiboController;
import me.kuku.yuq.controller.weibo.WeiboNotController;
import me.kuku.yuq.utils.BotUtils;

@com.IceCreamQAQ.YuWeb.annotation.WebController
@SuppressWarnings("unused")
public class WebController {
    @Action("groupMessage")
    public String groupMessage(String group, String msg){
        Group g = FunKt.getYuq().getGroups().get(Long.parseLong(group));
        if (g == null) return "Error: Group Not Found!";
        g.sendMessage(Message.Companion.toMessage(msg));
        return "OK!";
    }

    @Action("privateMessage")
    public String privateMessage(String qq, String msg){
        Friend f = FunKt.getYuq().getFriends().get(Long.parseLong(qq));
        if (f == null) return "Error: Friend Not Found!";
        f.sendMessage(Message.Companion.toMessage(msg));
        return "OK!";
    }

    @Action("tempMessage")
    public String tempMessage(String qq, String group, String msg){
        Group g = FunKt.getYuq().getGroups().get(Long.parseLong(group));
        if (g == null) return "Error: Group Not Found!";
        Member m = g.getMembers().get(Long.parseLong(qq));
        if (m == null) return "Error: Member Not Found!";
        return "OK!";
    }

    @Action("menu")
    public String menu(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("help", BotUtils.menu(MenuController.class));
        jsonObject.put("tool", BotUtils.menu(ToolController.class));
        jsonObject.put("bilibili", BotUtils.menu(BiliBiliLoginController.class, BiliBiliController.class));
        jsonObject.put("bot", BotUtils.menu(BotController.class));
        jsonObject.put("manage", BotUtils.menu(ManageNotController.class, ManageSuperAdminController.class, ManageAdminController.class));
        jsonObject.put("wy", BotUtils.menu(NeTeaseController.class, BindNeTeaseController.class));
        jsonObject.put("qq", BotUtils.menu(QQLoginController.class, BindQQController.class, QQJobController.class, QQQuickLoginController.class));
        jsonObject.put("setting", BotUtils.menu(SettingController.class));
        jsonObject.put("wb", BotUtils.menu(WeiboNotController.class, WeiboController.class));
        jsonObject.put("wf", BotUtils.menu(WarframeController.class));
        jsonObject.put("ark", BotUtils.menu(ArkNightsController.class));
        return jsonObject.toString();
    }
}