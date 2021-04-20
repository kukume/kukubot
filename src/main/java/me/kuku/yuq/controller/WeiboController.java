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
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.WeiboEntity;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.WeiboPojo;
import me.kuku.yuq.service.WeiboService;
import me.kuku.yuq.utils.ExecutorUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Action("加微博赞 {username}")
    @Synonym({"加微博评论 {username}", "加微博转发 {username}"})
    @QMsg(at = true)
    public String weiboAdd(WeiboEntity weiboEntity, @PathVar(0) String type, String username, ContextSession session, long qq, Group group) throws IOException {
        type = type.substring(3);
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

    @Action("删微博赞 {username}")
    @Synonym({"删微博评论 {username}", "删微博转发 {username}"})
    @QMsg(at = true)
    public String weiboDel(WeiboEntity weiboEntity, @PathVar(0) String type, String username){
        type = type.substring(3);
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

    @Action("查微博赞")
    @Synonym({"查微博评论", "查微博转发"})
    @QMsg(at = true, atNewLine = true)
    public String weiboList(WeiboEntity weiboEntity, @PathVar(0) String type){
        type = type.substring(3);
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

    @GroupController
    @PrivateController
    public static class BindWeiboController extends QQController {

        @Inject
        private WeiboLogic weiboLogic;
        @Inject
        private WeiboService weiboService;

        @Action("wblogin")
        public void wbLoginByQr(long qq, Group group) throws IOException {
            Map<String, String> map = weiboLogic.loginByQr1();
            group.sendMessage(FunKt.getMif().at(qq).plus("请使用微博APP扫码登录").plus(
                    FunKt.getMif().imageByUrl("https:" + map.get("url"))
            ));
            ExecutorUtils.execute(() -> {
                String id = map.get("id");
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Result<WeiboEntity> result = weiboLogic.loginByQr2(id);
                        if (result.getCode() == 200) {
                            WeiboEntity newWeiboEntity = result.getData();
                            WeiboEntity weiboEntity = weiboService.findByQQ(qq);
                            if (weiboEntity == null) weiboEntity = new WeiboEntity(qq, group.getId());
                            weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
                            weiboEntity.setGroup(group.getId());
                            weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
                            weiboService.save(weiboEntity);
                            group.sendMessage(FunKt.getMif().at(qq).plus("绑定微博信息成功！！"));
                            break;
                        } else if (result.getCode() == 500) {
                            group.sendMessage(FunKt.getMif().at(qq).plus(result.getMessage()));
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

//	@Action("wb {username} {password}")
//	public String wbLoginByPwd(String username, String password, ContextSession session, Contact qq) throws IOException {
//		Long group = null;
//		if (qq instanceof Member){
//			group = ((Member) qq).getGroup().getId();
//		}
//		Result<Map<String, String>> preparedLoginResult = weiboLogic.preparedLogin(username, password);
//		String door = null;
//		Map<String, String> preparedLoginMap = preparedLoginResult.getData();
//		if (preparedLoginResult.getCode() != 200){
//			String url = weiboLogic.getCaptchaUrl(preparedLoginMap.get("pcid"));
//			reply("请输入验证码，验证码地址： " + url + " ，如看不清，请重新打开网址即可更换验证码！！");
//			Message waitMessage = session.waitNextMessage();
//			door = Message.Companion.firstString(waitMessage);
//		}
//		Result<Map<String, String>> loginResult = weiboLogic.login(preparedLoginMap, door);
//		Integer code = loginResult.getCode();
//		Map<String, String> loginMap = loginResult.getData();
//		WeiboEntity weiboEntity = weiboService.findByQQ(qq.getId());
//		if (weiboEntity == null) weiboEntity = new WeiboEntity(qq.getId(), group);
//		if (code == 200){
//			WeiboEntity newWeiboEntity = weiboLogic.loginSuccess(loginMap.get("cookie"), loginMap.get("referer"), loginMap.get("url"));
//			weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
//			weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
//			weiboService.save(weiboEntity);
//			return "绑定或者更新微博成功！！";
//		}else if (code == 201){
//			reply("账号需要验证，请输入数字： 1、代表使用手机验证码进行验证；2、代表使用私聊验证进行验证");
//			int num;
//			while (true) {
//				Message numMessage = session.waitNextMessage();
//				String numStr = Message.Companion.firstString(numMessage);
//				String msg = "您输入的不为其中的数字，请重新输入！！";
//				try {
//					num = Integer.parseInt(numStr);
//					if (num != 1 && num != 2){
//						reply(msg);
//					}else break;
//				} catch (NumberFormatException e) {
//					e.printStackTrace();
//					reply(msg);
//				}
//			}
//			String token = loginMap.get("token");
//			if (num == 1){
//				Result<Map<String, String>> smsResult = weiboLogic.loginBySms1(token);
//				if (smsResult.getCode() == 200){
//					Map<String, String> smsMap = smsResult.getData();
//					reply("请输入短信验证码");
//					Message codeMessage = session.waitNextMessage(1000 * 60 * 3);
//					String smsCode = Message.Companion.firstString(codeMessage);
//					Result<WeiboEntity> smsFinallyResult = weiboLogic.loginBySms2(token, smsMap.get("phone"), smsCode);
//					if (smsFinallyResult.getCode() == 200){
//						WeiboEntity newWeiboEntity = smsFinallyResult.getData();
//						weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
//						weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
//						weiboService.save(weiboEntity);
//						return "绑定或者更新微博成功！！";
//					}else return smsFinallyResult.getMessage();
//				}else return smsResult.getMessage();
//			}else {
//				Result<WeiboEntity> privateResult = weiboLogic.loginByPrivateMsg(token);
//				if (privateResult.getCode() == 200){
//					WeiboEntity newWeiboEntity = privateResult.getData();
//					weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
//					weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
//					weiboService.save(weiboEntity);
//					return "绑定或者更新微博成功！！";
//				}else return privateResult.getMessage();
//			}
//		}else return loginResult.getMessage();
//	}

        @Action("wb {username} {password}")
        public String wbLoginByMobile(String username, String password, ContextSession session, Contact qq) throws IOException {
            Long group = null;
            if (qq instanceof Member){
                group = ((Member) qq).getGroup().getId();
            }
            Result<Map<String, String>> loginResult = weiboLogic.loginByMobile(username, password);
            Integer code = loginResult.getCode();
            WeiboEntity weiboEntity = weiboService.findByQQ(qq.getId());
            if (weiboEntity == null) weiboEntity = new WeiboEntity(qq.getId(), group);
            if (code == 201){
                reply("账号需要验证，请输入数字： 1、代表使用手机验证码进行验证；2、代表使用微博私信验证码进行验证");
                int num;
                while (true) {
                    Message numMessage = session.waitNextMessage();
                    String numStr = Message.Companion.firstString(numMessage);
                    String msg = "您输入的不为其中的数字，请重新输入！！";
                    try {
                        num = Integer.parseInt(numStr);
                        if (num != 1 && num != 2){
                            reply(msg);
                        }else break;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        reply(msg);
                    }
                }
                Map<String, String> map = loginResult.getData();
                String cookie = map.get("cookie");
                if (num == 1){
                    Result<String> smsResult = weiboLogic.loginByMobileSms1(map.get("phone"), cookie);
                    if (smsResult.isFailure()) return smsResult.getMessage();
                    reply("请输入短信验证码");
                    Message codeMessage = session.waitNextMessage(1000 * 60 * 3);
                    String smsCode = Message.Companion.firstString(codeMessage);
                    Result<WeiboEntity> result = weiboLogic.loginByMobileSms2(smsCode, cookie);
                    if (result.isSuccess()){
                        WeiboEntity newWeiboEntity = result.getData();
                        weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
                        weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
                        weiboService.save(weiboEntity);
                        return "绑定或者更新微博成功！！";
                    }else return result.getMessage();
                }else {
                    Result<String> smsResult = weiboLogic.loginByMobilePrivateMsg1(cookie);
                    if (smsResult.isFailure()) return smsResult.getMessage();
                    reply("请输入微博私信验证码");
                    Message codeMessage = session.waitNextMessage(1000 * 60 * 3);
                    String smsCode = Message.Companion.firstString(codeMessage);
                    Result<WeiboEntity> result = weiboLogic.loginByMobilePrivateMsg2(smsCode, cookie);
                    if (result.isSuccess()){
                        WeiboEntity newWeiboEntity = result.getData();
                        weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
                        weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
                        weiboService.save(weiboEntity);
                        return "绑定或者更新微博成功！！";
                    }else return result.getMessage();
                }
            }else if (code == 200){
                return "暂不支持不需要验证的账号，请使在群聊发送<wblogin>进行登录";
            }else return loginResult.getMessage();
        }
    }

    @GroupController
    public static class WeiboNotController {
        @Inject
        private WeiboLogic weiboLogic;

        @Action("热搜")
        @QMsg(at = true, atNewLine = true)
        public String hotSearch() throws IOException {
            List<String> list = weiboLogic.hotSearch();
            StringBuilder sb = new StringBuilder();
            for (String str: list){
                sb.append(str).append("\n");
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        }

        @Action("wbinfo {username}")
        @QMsg(at = true, atNewLine = true)
        public String weiboInfo(String username) throws IOException {
            Result<List<WeiboPojo>> idResult = weiboLogic.getIdByName(username);
            List<WeiboPojo> idList = idResult.getData();
            if (idList == null) return idResult.getMessage();
            return weiboLogic.getUserInfo(idList.get(0).getUserId());
        }
    }

}