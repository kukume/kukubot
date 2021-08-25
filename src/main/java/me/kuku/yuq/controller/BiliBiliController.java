package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.entity.Group;
import me.kuku.pojo.Result;
import me.kuku.utils.IOUtils;
import me.kuku.yuq.entity.BiliBiliEntity;
import me.kuku.yuq.entity.BiliBiliService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.logic.ToolLogic;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@GroupController
public class BiliBiliController {
	@Inject
	private BiliBiliLogic biliBiliLogic;
	@Inject
	private BiliBiliService biliBiliService;
	@Inject
	private JobManager jobManager;
	@Inject
	private ToolLogic toolLogic;

	@Action("哔哩哔哩二维码")
	public void biliBiliLoginByQr(Group group, Long qq, QqEntity qqEntity) {
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
		jobManager.registerTimer(() -> {
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
							BiliBiliEntity biliBiliEntity = biliBiliService.findByQqEntity(qqEntity);
							if (biliBiliEntity == null) biliBiliEntity = BiliBiliEntity.Companion.getInstance(qqEntity);
							BiliBiliEntity newBiliBiliEntity = result.getData();
							biliBiliEntity.setCookie(newBiliBiliEntity.getCookie());
							biliBiliEntity.setToken(newBiliBiliEntity.getToken());
							biliBiliEntity.setUserid(newBiliBiliEntity.getUserid());
							biliBiliService.save(biliBiliEntity);
							group.sendMessage(FunKt.getMif().at(qq).plus("绑定或者更新哔哩哔哩成功！！"));
							return;
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}, 0);
	}

	@Before(except = "biliBiliLoginByQr")
	public void before(QqEntity qqEntity, BotActionContext actionContext) {
		BiliBiliEntity biliEntity = biliBiliService.findByQqEntity(qqEntity);
		if (biliEntity == null)
			throw FunKt.getMif().at(qqEntity.getQq()).plus("您没有绑定哔哩哔哩账号，请发送<哔哩哔哩二维码>进行绑定！").toThrowable();
		actionContext.set("biliBiliEntity", biliEntity);
	}

	@Action("哔哩哔哩监控 {status}")
	@Synonym({"哔哩哔哩开播提醒 {status}", "哔哩哔哩任务 {status}"})
	@QMsg(at = true)
	public String biliBiliMonitor(BiliBiliEntity biliBiliEntity, boolean status, @PathVar(0) String type) {
		switch (type){
			case "哔哩哔哩监控": biliBiliEntity.setMonitor(status); break;
			case "哔哩哔哩任务": biliBiliEntity.setTask(status); break;
			case "哔哩哔哩开播提醒": biliBiliEntity.setLive(status); break;
		}
		biliBiliService.save(biliBiliEntity);
		return type + (status ? "开启成功" : "关闭成功");
	}

	@Action("哔哩哔哩举报 {bvId}")
	@QMsg(at = true)
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

	@Action("删除哔哩哔哩")
	@QMsg(at = true)
	public String delete(BiliBiliEntity biliBiliEntity){
		biliBiliService.delete(biliBiliEntity);
		return "删除哔哩哔哩信息成功！";
	}
}