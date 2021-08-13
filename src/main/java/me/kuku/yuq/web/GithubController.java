package me.kuku.yuq.web;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.YuWeb.annotation.WebController;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.GroupService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.util.List;

@WebController
public class GithubController {

	@Inject
	private GroupService groupService;

	@Action("github-webhook")
	public String webhook(JSONObject jsonObject){
		if (jsonObject == null) return "error!";
		JSONObject repository = jsonObject.getJSONObject("repository");
		String name = repository.getString("full_name");
		JSONObject headCommit = jsonObject.getJSONObject("headcommit");
		String msg = null;
		if (headCommit != null){
			String message = headCommit.getString("message");
			String timestamp = headCommit.getString("timestamp");
			msg = "检测到来自" + name + "的新commit\n" +
					"时间：" + timestamp + "\n" +
					"提交信息：" + message;
		}
		JSONObject issue = jsonObject.getJSONObject("issue");
		if (issue != null){
			String title = issue.getString("title");
			String time = issue.getString("updated_at");
			String body = issue.getString("body");
			String username = issue.getJSONObject("user").getString("login");
			String state = issue.getString("state");
			msg = "检测到来自" + name + "的issues\n" +
					"状态：" + state + "\n" +
					"昵称：" + username + "\n" +
					"时间：" + time + "\n" +
					"标题：" + title + "\n" +
					"内容：" + body;
			JSONObject comment = jsonObject.getJSONObject("comment");
			if (comment != null){
				String commentUsername = comment.getJSONObject("user").getString("login");
				String commentBody = comment.getString("body");
				msg += "\n回复者：" + commentUsername + "\n"
						+ "回复内容：" + commentBody;
			}
		}
		if (msg != null){
			List<GroupEntity> list = groupService.findByGithubPush(true);
			for (GroupEntity groupEntity : list) {
				BotUtils.sendMessage(groupEntity.getGroup(), msg);
			}
		}
		return "OK!";
	}

}
