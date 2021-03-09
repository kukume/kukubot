package me.kuku.yuq.controller.weibo;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.WeiboEntity;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.WeiboPojo;
import me.kuku.yuq.service.WeiboService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@GroupController
@SuppressWarnings("unused")
public class WeiboController {
    @Inject
    private WeiboLogic weiboLogic;
    @Inject
    private WeiboService weiboService;

    @Before
    public WeiboEntity before(long qq){
        WeiboEntity weiboEntity = weiboService.findByQQ(qq);
        if (weiboEntity == null) throw FunKt.getMif().at(qq).plus("您还未绑定微博，请先私聊机器人发送（wb 账号 密码）进行绑定").toThrowable();
        return weiboEntity;
    }

    @Action("微博关注监控 {status}")
    @QMsg(at = true)
    public String weiboMyMonitor(boolean status, WeiboEntity weiboEntity){
        weiboEntity.setMonitor(status);
        weiboService.save(weiboEntity);
        if (status) return "我的关注微博监控开启成功！！";
        else return "我的关注微博监控关闭成功！！";
    }

    @Action("微博/add/{type}/{username}")
    @QMsg(at = true)
    public String weiboAdd(WeiboEntity weiboEntity, String type, String username, ContextSession session, long qq, Group group) throws IOException {
        Result<List<WeiboPojo>> result = weiboLogic.getIdByName(username);
        List<WeiboPojo> list = result.getData();
        if (list == null) return result.getMessage();
        WeiboPojo weiboPojo = list.get(0);
        String id = weiboPojo.getUserId();
        String name = weiboPojo.getName();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        Message.Companion messageCompanion = Message.Companion;
        switch (type){
            case "赞":
                weiboEntity.setLikeJsonArray(weiboEntity.getLikeJsonArray().fluentAdd(jsonObject));
                break;
            case "评论":
                group.sendMessage(FunKt.getMif().at(qq).plus("请输入需要评论的内容！！"));
                String commentContent = messageCompanion.firstString(session.waitNextMessage());
                jsonObject.put("content", commentContent);
                weiboEntity.setCommentJsonArray(weiboEntity.getCommentJsonArray().fluentAdd(jsonObject));
                break;
            case "转发":
                group.sendMessage(FunKt.getMif().at(qq).plus("请输入需要转发的内容！！"));
                String forwardContent = messageCompanion.firstString(session.waitNextMessage());
                jsonObject.put("content", forwardContent);
                weiboEntity.setForwardJsonArray(weiboEntity.getForwardJsonArray().fluentAdd(jsonObject));
                break;
            default: return null;
        }
        weiboService.save(weiboEntity);
        return "添加微博用户<" + weiboPojo.getName() + ">的自动" + type + "成功";
    }

    private List<JSONObject> del(JSONArray jsonArray, String username){
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (username.equals(jsonObject.getString("name"))) list.add(jsonObject);
        }
        return list;
    }

    @Action("微博/del/{type}/{username}")
    @QMsg(at = true)
    public String weiboDel(WeiboEntity weiboEntity, String type, String username){
        switch (type){
            case "赞":
                JSONArray likeJsonArray = weiboEntity.getLikeJsonArray();
                weiboEntity.setLikeJsonArray(likeJsonArray.fluentRemoveAll(del(likeJsonArray, username)));
                break;
            case "评论":
                JSONArray commentJsonArray = weiboEntity.getCommentJsonArray();
                weiboEntity.setCommentJsonArray(commentJsonArray.fluentRemoveAll(del(commentJsonArray, username)));
                break;
            case "转发":
                JSONArray forwardJsonArray = weiboEntity.getForwardJsonArray();
                weiboEntity.setForwardJsonArray(forwardJsonArray.fluentRemoveAll(del(forwardJsonArray, username)));
                break;
            default: return null;
        }
        weiboService.save(weiboEntity);
        return "删除成功！！";
    }

    @Action("微博/list/{type}")
    @QMsg(at = true, atNewLine = true)
    public String weiboList(WeiboEntity weiboEntity, String type){
        StringBuilder sb = new StringBuilder();
        JSONArray jsonArray;
        switch (type){
            case "赞":
                sb.append("您的微博自动赞列表如下：").append("\n");
                jsonArray = weiboEntity.getLikeJsonArray();
                break;
            case "评论":
                sb.append("您的微博自动评论列表如下：").append("\n");
                jsonArray = weiboEntity.getCommentJsonArray();
                break;
            case "转发":
                sb.append("您的微博自动转发列表如下：").append("\n");
                jsonArray = weiboEntity.getForwardJsonArray();
                break;
            default: return null;
        }

        for (Object obj: jsonArray){
            JSONObject jsonObject = (JSONObject) obj;
            sb.append(jsonObject.getString("id")).append("-")
                    .append(jsonObject.getString("name")).append("-")
                    .append(jsonObject.getString("content"));
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}