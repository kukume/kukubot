package me.kuku.yuq.controller.arknights;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.BotActionContext;
import me.kuku.yuq.entity.ArkNightsEntity;
import me.kuku.yuq.logic.ArkNightsLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ArkNightsService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.DateTimeFormatterUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@GroupController
public class ArkNightsController {

	@Inject
	private ArkNightsService arkNightsService;
	@Inject
	private ArkNightsLogic arkNightsLogic;

	private final String pattern = "yyyy-MM-dd HH:mm:ss";

	@Before
	public ArkNightsEntity before(long qq){
		ArkNightsEntity arkNightsEntity = arkNightsService.findByQQ(qq);
		if (arkNightsEntity == null)
			throw FunKt.getMif().at(qq).plus("您还没有绑定arkNights，请私聊机器人发送<ark 账号 密码>进行绑定").toThrowable();
		else return arkNightsEntity;
	}

	@Action("ark充值记录")
	@QMsg(at = true, atNewLine = true)
	public String rechargeRecord(ArkNightsEntity arkNightsEntity) throws IOException {
		Result<List<Map<String, String>>> result = arkNightsLogic.rechargeRecord(arkNightsEntity.getCookie());
		if (result.getCode() == 200){
			StringBuilder sb = new StringBuilder().append("商品名称    金额    时间").append("\n");
			List<Map<String, String>> list = result.getData();
			for (Map<String, String> map : list) {
				sb.append(map.get("productName")).append("    ").append(map.get("amount")).append("    ")
						.append(DateTimeFormatterUtils.format(Long.parseLong(map.get("payTime")), pattern)).append("\n");
			}
			return BotUtils.removeLastLine(sb);
		}else return result.getMessage();
	}

	@Action("ark寻访记录")
	@QMsg(at = true, atNewLine = true)
	public String searchRecord(ArkNightsEntity arkNightsEntity, @PathVar(value = 1, type = PathVar.Type.Integer) Integer page) throws IOException {
		if (page == null) page = 1;
		Result<List<Map<String, String>>> result = arkNightsLogic.searchRecord(arkNightsEntity.getCookie(), page);
		if (result.getCode() == 200){
			StringBuilder sb = new StringBuilder().append("时间         获得干员").append("\n");
			List<Map<String, String>> list = result.getData();
			for (Map<String, String> map : list) {
				sb.append(DateTimeFormatterUtils.format(Long.parseLong(map.get("ts")), pattern)).append("   ").append(map.get("result")).append("\n");
			}
			return BotUtils.removeLastLine(sb);
		}else return result.getMessage();
	}

	@Action("ark源石记录")
	@QMsg(at = true, atNewLine = true)
	public String sourceRecord(ArkNightsEntity arkNightsEntity, @PathVar(value = 1, type = PathVar.Type.Integer) Integer page) throws IOException {
		if (page == null) page = 1;
		Result<List<Map<String, String>>> result = arkNightsLogic.sourceRecord(arkNightsEntity.getCookie(), page);
		if (result.getCode() == 200){
			StringBuilder sb = new StringBuilder().append("时间         变更     操作").append("\n");
			List<Map<String, String>> list = result.getData();
			for (Map<String, String> map : list) {
				sb.append(DateTimeFormatterUtils.format(Long.parseLong(map.get("ts")), pattern)).append("   ").append(map.get("coin")).append("     ").append(map.get("operation")).append("\n");
			}
			return BotUtils.removeLastLine(sb);
		}else return result.getMessage();
	}
}
