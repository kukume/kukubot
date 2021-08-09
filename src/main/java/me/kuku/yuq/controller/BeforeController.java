package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Catch;
import com.IceCreamQAQ.Yu.annotation.Global;
import com.IceCreamQAQ.Yu.cache.EhcacheHelp;
import com.IceCreamQAQ.Yu.entity.DoNone;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.BotActionContext;
import com.icecreamqaq.yuq.message.Message;
import kotlinx.coroutines.TimeoutCancellationException;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.GroupService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.entity.QqService;
import me.kuku.yuq.logic.ToolLogic;
import net.mamoe.mirai.contact.BotIsBeingMutedException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@GroupController
@PrivateController
public class BeforeController {

	@Inject
	private QqService qqService;
	@Inject
	private GroupService groupService;
	@Inject
	@Named("CommandCountOnTime")
	private EhcacheHelp<Integer> eh;
	@Inject
	private ToolLogic toolLogic;

	@Global
	@Before(weight = -1)
	public void nextParams(BotActionContext context, Long qq, Long group){
		QqEntity qqEntity = qqService.findByQq(qq);
		context.set("qqEntity", qqEntity);
		if (group != null){
			GroupEntity groupEntity = groupService.findByGroup(group);
			context.set("groupEntity", groupEntity);
		}
	}

	@Global
	@Before
	public void before(Message message, Long group, Long qq){
		if (group == null) return;
		List<String> list = message.toPath();
		if (list.size() == 0) return;
		String command = list.get(0);
		if (command.equals("指令限制") || command.equals("加指令限制")) return;
		GroupEntity groupEntity = groupService.findByGroup(group);
		if (groupEntity == null) return;
		Integer maxCount = groupEntity.getMaxCommandCountOnTime();
		if (maxCount == null) maxCount = -1;
		if (maxCount < 0) return;
		String key = "qq" + qq.toString() + command;
		Integer num = eh.get(key);
		if (num == null) num = 0;
		if (num >= maxCount) throw new DoNone();
		eh.set(key, ++num);
	}

	@Global
	@Before
	public void before(Message message, Long group){
		if (group == null) return;
		List<String> list = message.toPath();
		if (list.size() == 0) return;
		GroupEntity groupEntity = groupService.findByGroup(group);
		if (groupEntity == null) return;
		JSONObject jsonObject = groupEntity.getCommandLimitJson();
		String command = list.get(0);
		if (jsonObject.containsKey(command)){
			Integer maxCount = jsonObject.getInteger(command);
			if (maxCount < 0) return;
			String key = "group" + group.toString() + command;
			Integer num = eh.get(key);
			if (num == null) num = 0;
			if (num >= maxCount) throw new DoNone();
			eh.set(key, ++num);
		}
	}

	@Global
	@Catch(error = Exception.class)
	public void recording(Exception exception, long group, long qq){
		if (exception instanceof TimeoutCancellationException
				|| exception instanceof IOException || exception instanceof BotIsBeingMutedException){
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		try {
			String url = toolLogic.pasteUbuntu("exception", "java", sw.toString());
			FunKt.getYuq().getGroups().get(group).sendMessage(FunKt.getMif().at(qq).plus("程序出现异常了，异常如下：" + url));
		}catch (Exception ignore){
		}
	}

	@Global
	@Catch(error = IOException.class)
	public void interIO(IOException iOException, long qq, long group){
		FunKt.getYuq().getGroups().get(group).sendMessage(FunKt.getMif().at(qq).plus("出现io异常了，请重试！！"));
	}

	@Global
	@Catch(error = BotIsBeingMutedException.class)
	public void innerMuted(BotIsBeingMutedException e, long qq, long group){
		FunKt.getYuq().getGroups().get(group).get(qq).sendMessage(Message.Companion.toMessage("机器人被禁言了，不能发送消息啦！！"));
	}

}
