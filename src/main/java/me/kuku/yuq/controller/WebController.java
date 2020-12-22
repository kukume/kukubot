package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.entity.Friend;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;

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
}