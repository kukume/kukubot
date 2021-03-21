package top.cubik65536.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.mirai.MiraiBot;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.*;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.DCloudPojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.TeambitionPojo;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.MiHoYoService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
/**
 * SettingController
 * top.cubik65536.yuq.controller
 * CubikBot
 * <p>
 * Created by Cubik65536 on 2021-03-21.
 * Copyright © 2020-2021 Cubik Inc. All rights reserved.
 * <p>
 * Description:
 * History:
 * 1. 2021-03-21 [Cubik65536]: Create file SettingController;
 */

@PrivateController
@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class SettingController extends QQController {
    @Inject
    private GroupService groupService;
    @Config("YuQ.Mirai.bot.master")
    private String master;
    @Inject
    private QQLoginLogic qqLoginLogic;
    @Inject
    private OkHttpWebImpl web;
    @Inject
    private MiraiBot miraiBot;
    @Inject
    private ConfigService configService;
    @Inject
    private TeambitionLogic teambitionLogic;
    @Inject
    private DCloudLogic dCloudLogic;
    @Inject
    private CodeLogic codeLogic;
    @Inject
    @Named("fateAdm")
    private CodeLogic fateAdmCodeLogic;

    @Before
    public void before(long qq, BotActionContext actionContext){
        if (qq != Long.parseLong(master))
            throw Message.Companion.toMessage("您不是机器人主人，无法执行！！").toThrowable();
        actionContext.set("qqLoginEntity", BotUtils.toQQLoginEntity(web, miraiBot));
    }

    @Action("HaiZhiAi {apiKey}")
    @Synonym("ChatAi {apiKey}")
    public String settingChatAi(String apiKey){
        ConfigEntity configEntity = configService.findByType("ChatAi");
        if (configEntity == null) configEntity = new ConfigEntity("ChatAi");
        configEntity.setContent(apiKey);
        configService.save(configEntity);
        return "绑定海知智能机器人的apiKey成功！！";
    }

    private ConfigEntity getEntity(ConfigType configType){
        ConfigEntity configEntity = configService.findByType(configType.getType());
        if (configEntity == null) configEntity = new ConfigEntity(configType.getType());
        return configEntity;
    }

}

