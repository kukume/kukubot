package me.kuku.simbot.controller;

import catcode.StringTemplate;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.ListenGroup;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.simbot.logic.QqGroupLogic;
import me.kuku.simbot.logic.QqLoginLogic;
import me.kuku.simbot.logic.ToolLogic;
import me.kuku.simbot.pojo.GroupMember;
import me.kuku.utils.DateTimeFormatterUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.SimpleServiceMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@OnGroup
@ListenGroup("bot")
public class BotController {

	@Resource
	private QqLoginLogic qqLoginLogic;
	@Resource
	private QqGroupLogic qqGroupLogic;
	@Resource
	private ToolLogic toolLogic;
	@Resource
	private StringTemplate stringTemplate;
	@Resource
	private MessageContentBuilderFactory messageContentBuilderFactory;

//	private List<Long> notSpeakByDay = null;

	@RegexFilter("列出{{day}}天未发言")
	public String notSpeak(QqLoginEntity qqLoginEntity, GroupMsg groupMsg, String day) throws IOException {
		long group = groupMsg.getGroupInfo().getGroupCodeNumber();
		Result<List<GroupMember>> result = qqLoginLogic.groupMemberInfo(qqLoginEntity, group);
		if (result.getCode() == 200){
			List<GroupMember> list = result.getData();
//			List<Long> qqList = new ArrayList<>();
			StringBuilder sb = new StringBuilder().append("本群").append(day).append("天未发言的成员如下：").append("\n");
			for (GroupMember groupMember : list) {
				if ((System.currentTimeMillis() - groupMember.getLastTime()) / (1000 * 60 * 60 * 24) > Integer.parseInt(day)){
					sb.append(groupMember.getQq()).append("\n");
//					qqList.add(groupMember.getQq());
				}
			}
//			notSpeakByDay = qqList;
			return sb.deleteCharAt(sb.length() - 1).toString();
		}else return result.getMessage();
	}

	@Filter(value = "查询", anyAt = true, trim = true)
	public String queryInfo(QqLoginEntity qqLoginEntity, GroupMsg groupMsg) throws IOException {
		String qqq = groupMsg.getMsgContent().getCats("at").get(0).get("code");
		Result<GroupMember> result = qqGroupLogic.queryMemberInfo(qqLoginEntity,
				groupMsg.getGroupInfo().getGroupCodeNumber(), Long.valueOf(qqq));
		GroupMember groupMember = result.getData();
		if (groupMember == null) return result.getMessage();
		String pattern = "yyyy-MM-dd HH:mm:ss";
		return "群名片：" + groupMember.getGroupCard() + "\n" +
				"Q龄：" + groupMember.getAge() + "\n" +
				"入群时间：" + DateTimeFormatterUtils.format(groupMember.getJoinTime(), pattern) + "\n" +
				"最后发言时间：" + DateTimeFormatterUtils.format(groupMember.getLastTime(), pattern);
	}

	@RegexFilter("天气{{local}}")
	public String weather(String local, QqLoginEntity qqLoginEntity, GroupMsg groupMsg) throws IOException {
		Result<String> result = toolLogic.weather(local, qqLoginEntity.getCookie());
		if (result.getCode() == 200){
			Bot.getInstance(groupMsg.getBotInfo().getBotCodeNumber())
					.getGroup(groupMsg.getGroupInfo().getGroupCodeNumber())
					.sendMessage(new SimpleServiceMessage(146, result.getData()));
			return null;
		}else return result.getMessage();
	}

	@Filter("龙王")
	public void dragonKing(QqLoginEntity qqLoginEntity, GroupMsg groupMsg, MsgSender msgSender) throws IOException {
		MessageContentBuilder build = messageContentBuilderFactory.getMessageContentBuilder();
		long group = groupMsg.getGroupInfo().getGroupCodeNumber();
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		long botQq = groupMsg.getBotInfo().getBotCodeNumber();
		List<Map<String, String>> list = qqGroupLogic.groupHonor(qqLoginEntity, group, "talkAtIve");
		if (list.size() == 0) {
			msgSender.SENDER.sendGroupMsg(group, build.at(qq).text("昨天没有龙王！！").build());
			return;
		}
		Map<String, String> map = list.get(0);
		long resultQQ = Long.parseLong(map.get("qq"));
		if (botQq == resultQQ){
			String[] arr = {"呼风唤雨", "84消毒", "巨星排面"};
			msgSender.SENDER.sendGroupMsg(group, build.at(qq).text(arr[(int) (Math.random() * arr.length)]).build());
			return;
		}
		String[] urlArr = {
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/0bd0d4c6-0ebb-4811-ba06-a0d65c3a8ed3.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/32c1791e-0cb5-4888-a99f-dd8bdd654423.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/493dfe13-bebb-4cd7-8d77-d0bde395db68.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c4cfb6b0-1e67-4f23-9d6e-80a03fb5f91f.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/568877f3-f62b-4cc1-97ee-0d48da8dfb59.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/f39ebff2-03c0-4cee-8967-206562cc055e.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8bbf31d5-878b-4d42-9aa0-a41fd8e13ea6.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c3aa3d94-5cf7-47e1-ba56-db9116b1bcae.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/d13d84e5-e7fa-4d1b-ae6c-1413ffc78769.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/b465c944-8373-4d8c-beda-56eb7c24fa0b.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/b049eb61-dca3-4541-b3dd-c220ccd94595.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/682c4454-fc52-41c3-9c44-890aaa08c03d.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/00d716cf-f691-42ea-aa71-e28f18a3b4b3.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8635cd24-5d87-4fc8-b429-425e02b22849.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/7309fe37-7e34-4b7e-9304-5a1a854d251c.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c631afd3-9614-403c-a5a1-18413bbe3374.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/cfa9129d-e99d-491b-932d-e353ce7ca2d8.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/40960b38-781d-43b0-863b-8962a5342020.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c3e83c57-242a-4843-af51-85a84f7badaf.gif",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8e4d291b-e6ba-48d9-b8f9-3adc56291c27.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8bcad94b-aff5-4e81-af89-8a1007eda4ae.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/dc8403a0-caec-40e0-98a8-93abdb263712.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/1468ee00-a106-42c7-9ce3-0ced6b2ddc3e.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/959cd1ef-8731-4379-b1ad-0d3bf66e38c0.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/79c484e0-695c-49e9-9514-bcbe294ca7c6.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/f9b48126-fb7e-4482-b5ce-140294f57066.png",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/732a6387-2595-4c56-80f8-c52fce6214bb.jpg",
				"https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/bb768136-d96d-451d-891a-5f409f7fbff1.jpg"
		};
		String url = urlArr[(int) (Math.random() * urlArr.length)];
		MessageContent messageContent = build.at(resultQQ).image(url).text("龙王，已上位" + map.get("desc") + "，快喷水！！").build();
		msgSender.SENDER.sendGroupMsg(groupMsg, messageContent);
	}

}
