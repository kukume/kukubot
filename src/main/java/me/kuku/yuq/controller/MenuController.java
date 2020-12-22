package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Path;
import com.IceCreamQAQ.Yu.annotation.Synonym;
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

import java.lang.reflect.Method;

@GroupController
public class MenuController {

    @Action("help")
    public String help(){
        return menu(MenuController.class);
    }

    @Action("tool")
    public String tool(){
        return menu(ToolController.class);
    }

    @Action("bilibili")
    public String bl(){
        return menu(BiliBiliLoginController.class) + "\n" + menu(BiliBiliController.class);
    }

    @Action("bot")
    public String bot(){
        return menu(BotController.class);
    }

    @Action("manage")
    public String manage(){
        return menu(ManageNotController.class) + "\n" +
                menu(ManageSuperAdminController.class) + "\n" +
                menu(ManageAdminController.class);
    }

    @Action("wy")
    public String wy(){
        return menu(NeTeaseController.class) + "\n" +
                menu(BindNeTeaseController.class) + "\n";
    }

    @Action("qq")
    public String qq(){
        return menu(QQLoginController.class) + "\n" +
                menu(BindQQController.class) + "\n" +
                menu(QQJobController.class) + "\n" +
                menu(QQQuickLoginController.class);
    }

    @Action("setting")
    public String setting(){
        return menu(SettingController.class);
    }

    @Action("wb")
    public String wb(){
        return menu(WeiboNotController.class) + "\n" + menu(WeiboController.class);
    }

    @Action("wf")
    public String wf() { return menu(WarframeController.class);}

    private String menu(Class<?> clazz){
        StringBuilder sb = new StringBuilder();
        String first = "";
        Path path = clazz.getAnnotation(Path.class);
        if (path != null){
            first = path.value() + " ";
        }
        Method[] methods = clazz.getMethods();
        for (Method method: methods){
            Action action = method.getAnnotation(Action.class);
            if (action != null){
                sb.append(first).append(action.value()).append("\n");
            }
            Synonym synonym = method.getAnnotation(Synonym.class);
            if (synonym != null){
                String[] arr = synonym.value();
                for (String str: arr){
                    sb.append(first).append(str).append("\n");
                }
            }
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
