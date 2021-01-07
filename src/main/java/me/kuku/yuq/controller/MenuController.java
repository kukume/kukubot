package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.annotation.GroupController;
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

import java.util.List;

@GroupController
@SuppressWarnings("unused")
public class MenuController {

    @Action("help")
    public String help(){
        return parse("help", MenuController.class);
    }

    @Action("tool")
    public String tool(){
        return parse("tool", ToolController.class);
    }

    @Action("bilibili")
    public String bl(){
        return parse("bilibili", BiliBiliLoginController.class, BiliBiliController.class);
    }

    @Action("bot")
    public String bot(){
        return parse("bot", BotController.class);
    }

    @Action("manage")
    public String manage(){
        return parse("manage", ManageNotController.class, ManageSuperAdminController.class, ManageAdminController.class);
    }

    @Action("wy")
    public String wy(){
        return parse("网易", NeTeaseController.class, BindNeTeaseController.class);
    }

    @Action("qq")
    public String qq(){
        return parse("qq", QQLoginController.class, BindQQController.class, QQJobController.class, QQQuickLoginController.class);
    }

    @Action("setting")
    public String setting(){
        return parse("设置", SettingController.class);
    }

    @Action("wb")
    public String wb(){
        return parse("微博", WeiboNotController.class, WeiboController.class);
    }

    @Action("wf")
    public String wf() {
        return parse("wf",  WarframeController.class);
    }

    @Action("ark")
    public String ark(){
        return parse("ark", ArkNightsController.class);
    }

    @Action("菜单")
    public String menu(){
        return "https://api.kuku.me/menu";
    }


    private String parse(String name, Class<?>...classes){
        List<String> list = BotUtils.menu(classes);
        StringBuilder sb = new StringBuilder().append("╭┅═☆━━━━━━━").append(name).append("━━━┅╮").append("\n");
        for (int i = 0; i < list.size(); i+=2) {
            sb.append("||").append(list.get(i));
            if (i + 1 < list.size()){
                sb.append("   ").append(list.get(i + 1)).append("||");
            }else sb.append("||");
            if (i + 1 < list.size()){
                sb.append("\n");
            }
        }
        return sb.append("╰┅━━━━━━━━━━━━━★═┅╯").toString();
    }


}
