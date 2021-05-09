package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.alibaba.fastjson.JSON;
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
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.*;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.pojo.DCloudPojo;
import me.kuku.yuq.pojo.OfficePojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private ConfigService configService;
    @Inject
    private DCloudLogic dCloudLogic;
    @Inject
    private CodeLogic codeLogic;
    @Inject
    private BotLogic botLogic;

    @Before
    public void before(long qq, BotActionContext actionContext){
        if (qq != Long.parseLong(master))
            throw Message.Companion.toMessage("您不是机器人主人，无法执行！！").toThrowable();
        actionContext.set("qqLoginEntity", botLogic.getQQLoginEntity());
    }

    @Action("群开启 {groupNo}")
    @Synonym({"群关闭 {groupNo}"})
    public String groupOpen(long groupNo, @PathVar(0) String str){
        Map<Long, Group> groups = FunKt.getYuq().getGroups();
        if (groups.containsKey(groupNo)){
            GroupEntity groupEntity = groupService.findByGroup(groupNo);
            if (groupEntity == null) groupEntity = new GroupEntity(groupNo);
            groupEntity.setStatus(str.contains("开启"));
            groupService.save(groupEntity);
            return "机器人开启或者关闭成功！！";
        }else return "机器人并没有加入这个群！！";
    }

    @Action("同意入群 {groupNo}")
    public String agreeAddGroup(QQLoginEntity qqLoginEntity, long groupNo) throws IOException {
        List<Map<String, String>> groupMsgList = qqLoginLogic.getGroupMsgList(qqLoginEntity);
        Map<String, String> resultMap = null;
        for (Map<String, String> map: groupMsgList){
            if (String.valueOf(groupNo).equals(map.get("group"))){
                resultMap = map;
                break;
            }
        }
        if (resultMap == null) return "没有找到这个群号";
        return qqLoginLogic.operatingGroupMsg(qqLoginEntity, "agree", resultMap, null);
    }

    @Action("退群 {groupNo}")
    public String leaveGroup(long groupNo){
        Map<Long, Group> groups = FunKt.getYuq().getGroups();
        if (groups.containsKey(groupNo)){
            groups.get(groupNo).leave();
            return "退出群聊成功！！";
        }else return "机器人并没有加入这个群！！";
    }

    @Action("baiduai ocr")
    @Synonym({"baiduai contentCensor", "baiduai speech"})
    public String settingBaiduAI(ContextSession session, Contact qq, @PathVar(1) String type){
        qq.sendMessage(BotUtils.toMessage("请输入appId"));
        Message firstMessage = session.waitNextMessage();
        String appId = BotUtils.firstString(firstMessage);
        qq.sendMessage(BotUtils.toMessage("请输入appKey"));
        Message secondMessage = session.waitNextMessage();
        String appKey = BotUtils.firstString(secondMessage);
        qq.sendMessage(BotUtils.toMessage("请输入secretKey"));
        Message thirdMessage = session.waitNextMessage();
        String secretKey = BotUtils.firstString(thirdMessage);
        String jsonType;
        switch (type){
            case "ocr":
                jsonType = "baiduAIOcr";
                break;
            case "contentCensor":
                jsonType = "baiduAIContentCensor";
                break;
            case "speech":
                jsonType = "baiduAISpeech";
                break;
            default: return null;
        }
        ConfigEntity configEntity = configService.findByType("baiduAI");
        if (configEntity == null) configEntity = new ConfigEntity("baiduAI");
        JSONObject jsonObject = configEntity.getContentJsonObject();
        JSONObject innerJsonObject = jsonObject.getJSONObject(jsonType);
        if (innerJsonObject == null) innerJsonObject = new JSONObject();
        innerJsonObject.put("appId", appId);
        innerJsonObject.put("appKey", appKey);
        innerJsonObject.put("secretKey", secretKey);
        jsonObject.put(jsonType, innerJsonObject);
        configEntity.setContentJsonObject(jsonObject);
        configService.save(configEntity);
        return "绑定百度AI的信息成功！！";
    }

    @Action("lolicon {apiKey}")
    public String settingLoLiCon(String apiKey){
        ConfigEntity configEntity = configService.findByType("loLiCon");
        if (configEntity == null) configEntity = new ConfigEntity("loLiCon");
        configEntity.setContent(apiKey);
        configService.save(configEntity);
        return "绑定loLiCon的apiKey成功！！";
    }

    @Action("加超管 {groupNum} {qqNum}")
    @Synonym({"删超管 {groupNum} {qqNum}"})
    public String addSuperAdmin(long groupNum, Long qqNum, @PathVar(0) String str){
        Map<Long, Group> groups = FunKt.getYuq().getGroups();
        if (groups.containsKey(groupNum)) {
            GroupEntity groupEntity = groupService.findByGroup(groupNum);
            if (groupEntity == null) groupEntity = new GroupEntity(groupNum);
            if (str.startsWith("加"))
                groupEntity.setSuperAdminJsonArray(groupEntity.getSuperAdminJsonArray().fluentAdd(qqNum.toString()));
            else if (str.startsWith("删")){
                JSONArray superAdminJsonArray = groupEntity.getSuperAdminJsonArray();
                BotUtils.delManager(superAdminJsonArray, qqNum.toString());
                groupEntity.setSuperAdminJsonArray(superAdminJsonArray);
            }else return null;
            groupService.save(groupEntity);
            return String.format("添加{%s}群的{%s}为超管成功！！", groupNum, qqNum);
        }else return "机器人并没有加入这个群！！";
    }

    @Action("dcloud {email} {password}")
    public String addDCloud(String email, String password, ContextSession session) throws IOException {
        reply("请输入服务空间的spaceId！！");
        Message idMessage = session.waitNextMessage();
        String spaceId = BotUtils.firstString(idMessage);
        DCloudPojo dCloudPojo = dCloudLogic.getData();
        Result<String> identifyResult = codeLogic.identify("3", dCloudPojo.getCaptchaImage());
        if (identifyResult.isFailure()) return identifyResult.getMessage();
        Result<DCloudPojo> loginResult = dCloudLogic.login(dCloudPojo, email, password, identifyResult.getData() );
        if (loginResult.isFailure()) return loginResult.getMessage();
        dCloudPojo = loginResult.getData();
        ConfigEntity configEntity = configService.findByType("dCloud");
        if (configEntity == null) configEntity = new ConfigEntity("dCloud");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("password", password);
        jsonObject.put("cookie", dCloudPojo.getCookie());
        jsonObject.put("token", dCloudPojo.getToken());
        jsonObject.put("spaceId", spaceId);
        configEntity.setContentJsonObject(jsonObject);
        configService.save(configEntity);
        return "绑定dCloud成功！！";
    }

    @Action("图鉴 {username} {password}")
    public String bindTt(String username, String password){
        ConfigEntity configEntity = configService.findByType(ConfigType.IdentifyCode.getType());
        if (configEntity == null) configEntity = new ConfigEntity(ConfigType.IdentifyCode.getType());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("password", password);
        configEntity.setContentJsonObject(jsonObject);
        configService.save(configEntity);
        return "绑定图鉴成功！！";
    }

    @Action("saucenao {apiKey}")
    public String sauceNao(String apiKey){
        ConfigEntity configEntity = getEntity(ConfigType.SauceNao);
        configEntity.setContent(apiKey);
        configService.save(configEntity);
        return "绑定sauceNao成功！！";
    }

    @Action("ddocr {apiKey}")
    public String bindDdOcr(String apiKey){
        ConfigEntity configEntity = getEntity(ConfigType.DdOcrCode);
        configEntity.setContent(apiKey);
        configService.save(configEntity);
        return "绑定ddOcr成功！！";
    }

    @Action("绑全局")
    public String bindOffice(ContextSession session){
        reply("请输入该全局显示的名称");
        Message nameMessage = session.waitNextMessage();
        String name = BotUtils.firstString(nameMessage);
        ConfigEntity entity = getEntity(ConfigType.OFFICE_USER);
        String content = entity.getContent();
        List<OfficePojo> officeList;
        if (content == null) officeList = new ArrayList<>();
        else {
            officeList = JSON.parseArray(content, OfficePojo.class);
            long count = officeList.stream().filter(it -> it.getName().equals(name)).count();
            if (count != 0) return "该名称已存在，请重新输入";
        }
        reply("请输入clientId");
        Message clientIdMessage = session.waitNextMessage();
        String clientId = BotUtils.firstString( clientIdMessage);
        reply("请输入clientSecret");
        Message clientSecretMessage = session.waitNextMessage();
        String clientSecret = BotUtils.firstString(clientSecretMessage);
        reply("请输入tenantId");
        Message tenantIdMessage = session.waitNextMessage();
        String tenantId = BotUtils.firstString(tenantIdMessage);
        reply("请输入domain");
        Message domainMessage = session.waitNextMessage();
        String domain = BotUtils.messageToString(domainMessage);
        reply("请输入订阅显示名称和订阅ID，名称和ID以|分割，如果有多个订阅，请使用;分割");
        Message skuMessage = session.waitNextMessage(1000 * 60 * 5);
        String ss = BotUtils.firstString(skuMessage);
        String[] arr = ss.split(";");
        List<OfficePojo.Sku> list = new ArrayList<>();
        for (String sss: arr){
            OfficePojo.Sku sku = new OfficePojo.Sku();
            String[] arrr = sss.split("\\|");
            sku.setName(arrr[0]);
            sku.setId(arrr[1]);
            list.add(sku);
        }
        OfficePojo pojo = new OfficePojo();
        pojo.setClientId(clientId);
        pojo.setClientSecret(clientSecret);
        pojo.setTenantId(tenantId);
        pojo.setDomain(domain);
        pojo.setSku(list);
        pojo.setName(name);
        officeList.add(pojo);
        entity.setContent(JSON.toJSONString(officeList));
        configService.save(entity);
        return "绑定全局信息成功！";
    }

    @Action("删全局 {name}")
    public String delOffice(String name){
        ConfigEntity entity = getEntity(ConfigType.OFFICE_USER);
        String content = entity.getContent();
        List<OfficePojo> list = JSON.parseArray(content, OfficePojo.class);
        List<OfficePojo> delList = new ArrayList<>();
        list.forEach(it -> {
            if (it.getName().equals(name)) delList.add(it);
        });
        delList.forEach(list::remove);
        entity.setContent(JSON.toJSONString(list));
        configService.save(entity);
        return "删除全局信息成功！";
    }

    private ConfigEntity getEntity(ConfigType configType){
        ConfigEntity configEntity = configService.findByType(configType.getType());
        if (configEntity == null) configEntity = new ConfigEntity(configType.getType());
        return configEntity;
    }

}
