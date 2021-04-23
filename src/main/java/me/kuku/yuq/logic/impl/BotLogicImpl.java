package me.kuku.yuq.logic.impl;

import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.mirai.MiraiBot;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.BotLogic;
import me.kuku.yuq.utils.QQUtils;
import net.mamoe.mirai.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;

@SuppressWarnings("unused")
public class BotLogicImpl implements BotLogic {
    @Inject
    private OkHttpWebImpl web;
    @Inject
    private MiraiBot miraiBot;
    @Config("YuQ.Mirai.user.qq")
    private String qqStr;

    private Integer expireTime = null;
    private QQLoginEntity qqLoginEntity = null;

    private final Logger log = LoggerFactory.getLogger(BotLogicImpl.class);

    @Override
    public QQLoginEntity getQQLoginEntity(){
        int nowTime = Math.toIntExact(System.currentTimeMillis() / 1000);
        if (expireTime == null || nowTime < expireTime) {
            long qq = Long.parseLong(qqStr);
            Bot bot = Bot.getInstance(qq);
            Field[] fields = bot.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("client")) {
                    field.setAccessible(true);
                    try {
                        Object client = field.get(bot);
                        Method[] methods = field.getType().getMethods();
                        for (Method method : methods) {
                            if (method.getName().equals("getWLoginSigInfo")) {
                                method.setAccessible(true);
                                Object s = method.invoke(client);
                                String jsonStr = JSON.toJSONString(s);
                                JSONObject jsonObject = JSON.parseObject(jsonStr);
                                JSONObject sKeyJsonObject = jsonObject.getJSONObject("sKey");
                                String sKey = de(sKeyJsonObject.getString("data"));
                                Integer expireTime = sKeyJsonObject.getInteger("expireTime");
                                String superKey = jsonObject.getString("superKey");
                                String superToken = QQUtils.getToken(superKey).toString();
                                JSONObject psKeyJsonObject = jsonObject.getJSONObject("psKeyMap");
                                JSONObject qZoneJsonObject = psKeyJsonObject.getJSONObject("qzone.qq.com");
                                String psKey = de(qZoneJsonObject.getString("data"));
                                Integer anotherExpireTime = qZoneJsonObject.getInteger("expireTime");
                                this.expireTime = Math.min(expireTime, anotherExpireTime);
                                String qunPsKey = de(psKeyJsonObject.getJSONObject("qun.qq.com").getString("data"));
                                qqLoginEntity =  new QQLoginEntity(null, qq, 0L, "", sKey, psKey, qunPsKey, superKey,
                                        superToken, null, true);
                                log.info("已从mirai获取到cookie");
                            }
                        }
                    } catch (Exception e) {
                        log.warn("获取cookie失败");
                        e.printStackTrace();
                        return new QQLoginEntity();
                    }
                }
            }
        }else log.info("已从缓存中获取到cookie");
        return qqLoginEntity;
    }

    private String de(String data){
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(data));
    }

}
