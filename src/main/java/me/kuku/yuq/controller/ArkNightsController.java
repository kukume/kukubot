package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import me.kuku.yuq.entity.QQBindEntity;
import me.kuku.yuq.logic.ArkNightsLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.QQBindService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@GroupController
public class ArkNightsController {

	@Inject
	private QQBindService qqBindService;
	@Inject
	private ArkNightsLogic arkNightsLogic;

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Before
	public void before(long qq, BotActionContext actionContext){
		QQBindEntity qqBindEntity = qqBindService.findByQQ(qq);
		if (qqBindEntity == null || qqBindEntity.getArkNightsCookie() == null)
			throw FunKt.getMif().at(qq).plus("您还没有绑定arkNights的cookie，请私聊机器人发送<ark cookie>进行绑定").toThrowable();
		else actionContext.set("cookie", qqBindEntity.getArkNightsCookie());
	}

	@Action("ark充值记录")
	@QMsg(at = true, atNewLine = true)
	public String rechargeRecord(String cookie) throws IOException {
		Result<List<Map<String, String>>> result = arkNightsLogic.rechargeRecord(cookie);
		if (result.getCode() == 200){
			StringBuilder sb = new StringBuilder().append("商品名称    金额    时间").append("\n");
			List<Map<String, String>> list = result.getData();
			for (Map<String, String> map : list) {
				sb.append(map.get("productName")).append("    ").append(map.get("amount")).append("    ").append(sdf.format(new Date(Long.parseLong(map.get("payTime"))))).append("\n");
			}
			return BotUtils.removeLastLine(sb);
		}else return result.getMessage();
	}

	@Action("ark寻访记录")
	@QMsg(at = true, atNewLine = true)
	public String searchRecord(String cookie, @PathVar(value = 1, type = PathVar.Type.Integer) Integer page) throws IOException {
		if (page == null) page = 1;
		Result<List<Map<String, String>>> result = arkNightsLogic.searchRecord(cookie, page);
		if (result.getCode() == 200){
			StringBuilder sb = new StringBuilder().append("时间         获得干员").append("\n");
			List<Map<String, String>> list = result.getData();
			for (Map<String, String> map : list) {
				sb.append(sdf.format(new Date(Long.parseLong(map.get("ts"))))).append("   ").append(map.get("result")).append("\n");
			}
			return BotUtils.removeLastLine(sb);
		}else return result.getMessage();
	}

	@Action("ark源石记录")
	@QMsg(at = true, atNewLine = true)
	public String sourceRecord(String cookie, @PathVar(value = 1, type = PathVar.Type.Integer) Integer page) throws IOException {
		if (page == null) page = 1;
		Result<List<Map<String, String>>> result = arkNightsLogic.sourceRecord(cookie, page);
		if (result.getCode() == 200){
			StringBuilder sb = new StringBuilder().append("时间         变更     操作").append("\n");
			List<Map<String, String>> list = result.getData();
			for (Map<String, String> map : list) {
				sb.append(sdf.format(new Date(Long.parseLong(map.get("ts"))))).append("   ").append(map.get("coin")).append("     ").append(map.get("operation")).append("\n");
			}
			return BotUtils.removeLastLine(sb);
		}else return result.getMessage();
	}
}
