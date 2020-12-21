package me.kuku.yuq.controller.weibo;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.WeiboEntity;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.WeiboService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@GroupController
@PrivateController
@SuppressWarnings("unused")
public class BindWeiboController extends QQController {

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
		new Thread(() -> {
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
					}else if (result.getCode() == 500){
						group.sendMessage(FunKt.getMif().at(qq).plus(result.getMessage()));
						break;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Action("wb {username} {password}")
	public String wbLoginByPwd(String username, String password, ContextSession session, Contact qq) throws IOException {
		Long group = null;
		if (qq instanceof Member){
			group = ((Member) qq).getGroup().getId();
		}
		Result<Map<String, String>> preparedLoginResult = weiboLogic.preparedLogin(username, password);
		String door = null;
		Map<String, String> preparedLoginMap = preparedLoginResult.getData();
		if (preparedLoginResult.getCode() != 200){
			String url = weiboLogic.getCaptchaUrl(preparedLoginMap.get("pcid"));
			reply("请输入验证码，验证码地址： " + url + " ，如看不清，请重新打开网址即可更换验证码！！");
			Message waitMessage = session.waitNextMessage();
			door = Message.Companion.firstString(waitMessage);
		}
		Result<Map<String, String>> loginResult = weiboLogic.login(preparedLoginMap, door);
		Integer code = loginResult.getCode();
		Map<String, String> loginMap = loginResult.getData();
		WeiboEntity weiboEntity = weiboService.findByQQ(qq.getId());
		if (weiboEntity == null) weiboEntity = new WeiboEntity(qq.getId(), group);
		if (code == 200){
			WeiboEntity newWeiboEntity = weiboLogic.loginSuccess(loginMap.get("cookie"), loginMap.get("referer"), loginMap.get("url"));
			weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
			weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
			weiboService.save(weiboEntity);
			return "绑定或者更新微博成功！！";
		}else if (code == 201){
			reply("账号需要验证，请输入数字： 1、代表使用手机验证码进行验证；2、代表使用私聊验证进行验证");
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
			String token = loginMap.get("token");
			if (num == 1){
				Result<Map<String, String>> smsResult = weiboLogic.loginBySms1(token);
				if (smsResult.getCode() == 200){
					Map<String, String> smsMap = smsResult.getData();
					reply("请输入短信验证码");
					Message codeMessage = session.waitNextMessage(1000 * 60 * 3);
					String smsCode = Message.Companion.firstString(codeMessage);
					Result<WeiboEntity> smsFinallyResult = weiboLogic.loginBySms2(token, smsMap.get("phone"), smsCode);
					if (smsFinallyResult.getCode() == 200){
						WeiboEntity newWeiboEntity = smsFinallyResult.getData();
						weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
						weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
						weiboService.save(weiboEntity);
						return "绑定或者更新微博成功！！";
					}else return smsFinallyResult.getMessage();
				}else return smsResult.getMessage();
			}else {
				Result<WeiboEntity> privateResult = weiboLogic.loginByPrivateMsg(token);
				if (privateResult.getCode() == 200){
					WeiboEntity newWeiboEntity = privateResult.getData();
					weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
					weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
					weiboService.save(weiboEntity);
					return "绑定或者更新微博成功！！";
				}else return privateResult.getMessage();
			}
		}else return loginResult.getMessage();
	}
}
