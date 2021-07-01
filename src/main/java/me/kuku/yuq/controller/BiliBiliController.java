package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Synonym;
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
import me.kuku.pojo.Result;
import me.kuku.utils.IOUtils;
import me.kuku.yuq.entity.BiliBiliEntity;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.BiliBiliPojo;
import me.kuku.yuq.service.BiliBiliService;
import me.kuku.yuq.utils.ExecutorUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@GroupController
@SuppressWarnings("unused")
public class BiliBiliController {
    @Inject
    private BiliBiliLogic biliBiliLogic;
    @Inject
    private BiliBiliService biliBiliService;

    @Before
    public void before(long qq, BotActionContext actionContext){
        BiliBiliEntity biliEntity = biliBiliService.findByQQ(qq);
        if (biliEntity == null)
            throw FunKt.getMif().at(qq).plus("您还没有绑定哔哩哔哩账号，无法继续！！！，如需绑定请发送bllogin qr").toThrowable();
        actionContext.set("biliBiliEntity", biliEntity);
    }

//    @Action("哔哩哔哩/add/{type}/{username}")
    @Action("加哔哩哔哩开播提醒 {username}")
    @Synonym({"加哔哩哔哩赞 {username}", "加哔哩哔哩评论 {username}", "加哔哩哔哩转发 {username}",
            "加哔哩哔哩投硬币 {username}", "加哔哩哔哩收藏 {username}"})
    public void biliBiliAdd(BiliBiliEntity biliBiliEntity, @PathVar(0) String type, String username, ContextSession session, Long qq, Group group) throws IOException {
        type = type.substring(5);
        Result<List<BiliBiliPojo>> result = biliBiliLogic.getIdByName(username);
        List<BiliBiliPojo> list = result.getData();
        if (list == null){
            group.sendMessage(FunKt.getMif().at(qq).plus("该用户不存在！！"));
            return;
        }
        BiliBiliPojo biliBiliPojo = list.get(0);
        String name = biliBiliPojo.getName();
        long id = Long.parseLong(biliBiliPojo.getUserId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        switch (type){
            case "开播提醒":
                biliBiliEntity.setLiveJsonArray(biliBiliEntity.getLiveJsonArray().fluentAdd(jsonObject));
                group.sendMessage(FunKt.getMif().at(qq).plus("添加" + name + "的开播提醒成功！！"));
                break;
            case "赞":
                biliBiliEntity.setLikeJsonArray(biliBiliEntity.getLikeJsonArray().fluentAdd(jsonObject));
                group.sendMessage(FunKt.getMif().at(qq).plus("添加" + name + "的自动赞成功！！"));
                break;
            case "评论":
                group.sendMessage(FunKt.getMif().at(qq).plus("请输入需要评论的内容！！"));
                Message message = session.waitNextMessage();
                String content = Message.Companion.firstString(message);
                jsonObject.put("content", content);
                biliBiliEntity.setCommentJsonArray(
                        biliBiliEntity.getCommentJsonArray().fluentAdd(content)
                );
                group.sendMessage(FunKt.getMif().at(qq).plus("添加" + name + "的自动评论成功！！"));
                break;
            case "转发":
                group.sendMessage(FunKt.getMif().at(qq).plus("请输入需要转发的内容"));
                String forwardContent = Message.Companion.firstString(session.waitNextMessage());
                jsonObject.put("content", forwardContent);
                biliBiliEntity.setForwardJsonArray(
                        biliBiliEntity.getForwardJsonArray().fluentAdd(jsonObject)
                );
                group.sendMessage(FunKt.getMif().at(qq).plus("添加" + name + "的自动转发成功！！"));
                break;
            case "投硬币":
                biliBiliEntity.setTossCoinJsonArray(
                        biliBiliEntity.getTossCoinJsonArray().fluentAdd(jsonObject)
                );
                group.sendMessage(FunKt.getMif().at(qq).plus("添加" + name + "的自动转发成功！！"));
                break;
            case "收藏":
                group.sendMessage(FunKt.mif.at(qq).plus("请输入需要自动收藏的收藏夹的名称"));
                String faContent = Message.Companion.firstString(session.waitNextMessage());
                jsonObject.put("content", faContent);
                biliBiliEntity.setForwardJsonArray(
                        biliBiliEntity.getForwardJsonArray().fluentAdd(jsonObject)
                );
                group.sendMessage(FunKt.getMif().at(qq).plus("添加" + name + "的自动收藏成功！！"));
                break;
            default: return;
        }
        biliBiliService.save(biliBiliEntity);
    }

    private List<JSONObject> del(JSONArray jsonArray, String username){
        List<JSONObject> list = new ArrayList<>();
        for (Object obj: jsonArray){
            JSONObject jsonObject = (JSONObject) obj;
            if (username.equals(jsonObject.getString("name"))) list.add(jsonObject);
        }
        return list;
    }

//    @Action("哔哩哔哩/del/{type}/{username}")
    @Action("删哔哩哔哩开播提醒 {username}")
    @Synonym({"删哔哩哔哩赞 {username}", "删哔哩哔哩评论 {username}",
            "删哔哩哔哩转发 {username}", "删哔哩哔哩投硬币 {username}", "删哔哩哔哩收藏 {username}"})
    @QMsg(at = true)
    public String biliBiliDel(BiliBiliEntity biliBiliEntity, @PathVar(0) String type, String username){
        type = type.substring(5);
        switch (type){
            case "开播提醒":
                JSONArray liveJsonArray = biliBiliEntity.getLiveJsonArray();
                biliBiliEntity.setLiveJsonArray(liveJsonArray.fluentRemoveAll(del(liveJsonArray, username)));
                break;
            case "赞":
                JSONArray likeJsonArray = biliBiliEntity.getLikeJsonArray();
                biliBiliEntity.setLiveJsonArray(likeJsonArray.fluentRemoveAll(del(likeJsonArray, username)));
                break;
            case "评论":
                JSONArray commentJsonArray = biliBiliEntity.getCommentJsonArray();
                biliBiliEntity.setCommentJsonArray(commentJsonArray.fluentRemoveAll(del(commentJsonArray, username)));
                break;
            case "转发":
                JSONArray forwardJsonArray = biliBiliEntity.getForwardJsonArray();
                biliBiliEntity.setForwardJsonArray(forwardJsonArray.fluentRemoveAll(del(forwardJsonArray, username)));
                break;
            case "投硬币":
                JSONArray tossCoinJsonArray = biliBiliEntity.getTossCoinJsonArray();
                biliBiliEntity.setTossCoinJsonArray(
                        tossCoinJsonArray.fluentRemoveAll(del(tossCoinJsonArray, username))
                );
                break;
            case "收藏":
                JSONArray favoritesJsonArray = biliBiliEntity.getFavoritesJsonArray();
                biliBiliEntity.setFavoritesJsonArray(
                        favoritesJsonArray.fluentRemoveAll(del(favoritesJsonArray, username))
                );
                break;
            default: return null;
        }
        biliBiliService.save(biliBiliEntity);
        return "删除成功！！";
    }

//    @Action("哔哩哔哩/list/{type}")
    @Action("查哔哩哔哩开播提醒")
    @Synonym({"查哔哩哔哩赞", "查哔哩哔哩评论", "查哔哩哔哩转发", "查哔哩哔哩投硬币", "查哔哩哔哩收藏"})
    @QMsg(at = true, atNewLine = true)
    public String biliBiliList(BiliBiliEntity biliBiliEntity, @PathVar(0) String type){
        type = type.substring(5);
        StringBuilder sb = new StringBuilder();
        JSONArray jsonArray;
        switch (type){
            case "开播提醒":
                sb.append("您的开播提醒列表如下：").append("\n");
                jsonArray = biliBiliEntity.getLiveJsonArray();
                break;
            case "赞":
                sb.append("您的自动赞列表如下：").append("\n");
                jsonArray = biliBiliEntity.getLikeJsonArray();
                break;
            case "评论":
                sb.append("您的自动评论列表如下：").append("\n");
                jsonArray = biliBiliEntity.getCommentJsonArray();
                break;
            case "转发":
                sb.append("您的自动转发列表如下：").append("\n");
                jsonArray = biliBiliEntity.getForwardJsonArray();
                break;
            case "投硬币":
                sb.append("您的自动投硬币列表如下：").append("\n");
                jsonArray = biliBiliEntity.getTossCoinJsonArray();
                break;
            case "收藏":
                sb.append("您的自动收藏列表如下：").append("\n");
                jsonArray = biliBiliEntity.getFavoritesJsonArray();
                break;
            default: return null;
        }
        for (Object obj: jsonArray){
            JSONObject jsonObject = (JSONObject) obj;
            sb.append(jsonObject.getString("id"))
                    .append("-")
                    .append(jsonObject.getString("name"))
                    .append("-")
                    .append(jsonObject.getString("content"))
                    .append("\n");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Action("哔哩哔哩关注监控 {status}")
    @QMsg(at = true)
    public String biliBiliMonitor(BiliBiliEntity biliBiliEntity, boolean status){
        biliBiliEntity.setMonitor(status);
        biliBiliService.save(biliBiliEntity);
        String ss = "关闭";
        if (status) ss = "开启";
        return "哔哩哔哩我的关注监控已" + ss;
    }

    @Action("哔哩哔哩任务 {status}")
    @QMsg(at = true)
    public String biliBiliTask(BiliBiliEntity biliBiliEntity, boolean status){
        biliBiliEntity.setTask(status);
        biliBiliService.save(biliBiliEntity);
        String ss = "关闭";
        if (status) ss = "开启";
        return "哔哩哔哩定时任务已" + ss;
    }

    @Action("哔哩哔哩举报 {bvId}")
    public String report(BiliBiliEntity biliBiliEntity, String bvId, long qq, Group group,
                         @PathVar(value = 2, type = PathVar.Type.Integer) Integer page) throws IOException {
        group.sendMessage(FunKt.getMif().at(qq).plus("正在为您举报中！！"));
        if (page == null) page = 1;
        String oid = biliBiliLogic.getOidByBvId(bvId);
        List<Map<String, String>> list = biliBiliLogic.getReplay(biliBiliEntity, oid, page);
        String msg = null;
        for (Map<String, String> map : list) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            msg = biliBiliLogic.reportComment(biliBiliEntity, oid, map.get("id"), 8);
        }
        return msg;
    }

    @GroupController
    @PrivateController
    public static class BiliBiliLoginController {
        @Inject
        private BiliBiliLogic biliBiliLogic;
        @Inject
        private BiliBiliService biliBiliService;
        @Inject
        private ToolLogic toolLogic;

        @Action("bllogin qr")
        @Synonym({"bilibililogin qr"})
        public void biliBiliLoginByQr(Group group, Long qq) {
            InputStream is = null;
            String url;
            AtomicInteger i = new AtomicInteger();
            try {
                url = biliBiliLogic.loginByQr1();
                is = toolLogic.creatQr(url);
                group.sendMessage(FunKt.getMif().at(qq).plus("请使用哔哩哔哩APP扫码登录：")
                        .plus(FunKt.getMif().imageByInputStream(is)));
            } catch (IOException e) {
                group.sendMessage(FunKt.getMif().at(qq).plus("二维码获取失败，请重试！！"));
                return;
            } finally {
                IOUtils.close(is);
            }
            ExecutorUtils.execute(() -> {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i.incrementAndGet() >= 20) {
                        group.sendMessage(FunKt.getMif().at(qq).plus("您的二维码已失效！！"));
                        break;
                    }
                    try {
                        Result<BiliBiliEntity> result = biliBiliLogic.loginByQr2(url);
                        switch (result.getCode()) {
                            case 500:
                                group.sendMessage(FunKt.getMif().at(qq).plus(result.getMessage()));
                                return;
                            case 200:
                                BiliBiliEntity biliBiliEntity = biliBiliService.findByQQ(qq);
                                if (biliBiliEntity == null) biliBiliEntity = new BiliBiliEntity(qq, group.getId());
                                BiliBiliEntity newBiliBiliEntity = result.getData();
                                biliBiliEntity.setCookie(newBiliBiliEntity.getCookie());
                                biliBiliEntity.setToken(newBiliBiliEntity.getToken());
                                biliBiliEntity.setUserId(newBiliBiliEntity.getUserId());
                                biliBiliService.save(biliBiliEntity);
                                group.sendMessage(FunKt.getMif().at(qq).plus("绑定或者更新哔哩哔哩成功！！"));
                                return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            });
        }

        @Action("bilibililogin pwd {username} {password}")
        public String loginByPassword(String username, String password, Contact qq) throws IOException {
            Long group = null;
            if (qq instanceof Member){
                group = ((Member) qq).getGroup().getId();
            }
            Result<BiliBiliEntity> result = biliBiliLogic.loginByPassword(username, password);
            if (result.isFailure()) return result.getMessage();
            else {
                BiliBiliEntity biliBiliEntity = biliBiliService.findByQQ(qq.getId());
                if (biliBiliEntity == null) biliBiliEntity = new BiliBiliEntity(qq.getId(), group);
                BiliBiliEntity newBiliBiliEntity = result.getData();
                biliBiliEntity.setCookie(newBiliBiliEntity.getCookie());
                biliBiliEntity.setToken(newBiliBiliEntity.getToken());
                biliBiliEntity.setUserId(newBiliBiliEntity.getUserId());
                biliBiliService.save(biliBiliEntity);
                return "绑定或者更新哔哩哔哩成功！！";
            }
        }
    }
}
