package me.kuku.yuq.logic.impl;

import com.IceCreamQAQ.Yu.util.OkHttpWebImpl;
import com.icecreamqaq.yuq.mirai.MiraiBot;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.BotLogic;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;

public class BotLogicImpl implements BotLogic {
    @Inject
    private OkHttpWebImpl web;
    @Inject
    private MiraiBot miraiBot;

    @Override
    public QQLoginEntity getQQLoginEntity(){
        return BotUtils.toQQLoginEntity(web, miraiBot);
    }

}
