package me.kuku.yuq.controller.qqlogin;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Path;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.controller.BotActionContext;
import me.kuku.yuq.entity.BiliBiliEntity;
import me.kuku.yuq.entity.MotionEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.entity.WeiboEntity;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.logic.LeXinMotionLogic;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.BiliBiliService;
import me.kuku.yuq.service.MotionService;
import me.kuku.yuq.service.QQLoginService;
import me.kuku.yuq.service.WeiboService;

import javax.inject.Inject;
import java.io.IOException;

@GroupController
public class QQQuickLoginController {

	@Inject
	private QQLoginService qqLoginService;
	@Inject
	private WeiboService weiboService;
	@Inject
	private BiliBiliService biliBiliService;
	@Inject
	private MotionService motionService;
	@Inject
	private WeiboLogic weiboLogic;
	@Inject
	private BiliBiliLogic biliBiliLogic;
	@Inject
	private LeXinMotionLogic leXinMotionLogic;

	@Before
	public void before(long qq, BotActionContext actionContext){
		QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qq);
		if (qqLoginEntity == null) throw FunKt.getMif().at(qq).plus("您还没有绑定QQ号，请绑定后再试！！<群聊发送<qqlogin qr>或者私聊发送<qqlogin pwd>>").toThrowable();
		else if (!qqLoginEntity.getStatus()) throw FunKt.getMif().at(qq).plus("您的QQ已失效，请更新后再试！！").toThrowable();
		else actionContext.set("qqLoginEntity", qqLoginEntity);
	}

	@Action("weibo")
	public String weiboLogin(QQLoginEntity qqLoginEntity, long qq, long group) throws IOException {
		WeiboEntity weiboEntity = weiboService.findByQQ(qq);
		if (weiboEntity == null) weiboEntity = new WeiboEntity(qq, group);
		Result<WeiboEntity> result = weiboLogic.loginByQQ(qqLoginEntity);
		if (result.getCode() == 200){
			WeiboEntity newWeiboEntity = result.getData();
			weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
			weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
			weiboService.save(weiboEntity);
			return "绑定或者更新微博成功！！";
		}else return result.getMessage();
	}

	@Action("bilibili")
	@Synonym("bl")
	public String biliBiliLogin(QQLoginEntity qqLoginEntity, long qq, long group){
		BiliBiliEntity biliBiliEntity = biliBiliService.findByQQ(qq);
		if (biliBiliEntity == null) biliBiliEntity = new BiliBiliEntity(qq, group);
//		biliBiliLogic.
		return "";
	}

	@Action("qqquicklogin lexin")
	public String leXinLogin(QQLoginEntity qqLoginEntity, long qq, long group) throws IOException {
		MotionEntity motionEntity = motionService.findByQQ(qq);
		if (motionEntity == null) motionEntity = new MotionEntity(qq);
		Result<MotionEntity> result = leXinMotionLogic.loginByQQ(qqLoginEntity);
		if (result.getCode() == 200){
			MotionEntity newMotionEntity = result.getData();
			motionEntity.setLeXinCookie(newMotionEntity.getLeXinCookie());
			motionEntity.setLeXinUserId(newMotionEntity.getLeXinUserId());
			motionEntity.setLeXinAccessToken(newMotionEntity.getLeXinAccessToken());
			motionService.save(motionEntity);
			return "绑定或者更新乐心运行成功！！";
		}else return result.getMessage();
	}


}
