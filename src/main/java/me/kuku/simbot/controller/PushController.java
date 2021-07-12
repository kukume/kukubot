package me.kuku.simbot.controller;

import love.forte.simbot.bot.*;
import me.kuku.pojo.Result;
import me.kuku.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/push")
public class PushController {

	@Autowired
	private BotRegistrar botRegistrar;
	@Autowired
	private BotManager botManager;
	@Autowired
	private BotDestroyer botDestroyer;

	@PostMapping("/login")
	@ResponseBody
	public Result<?> login(String qq, String password){
		PairBotVerifyInfo.BasicBotVerifyInfo basicBotVerifyInfo = new PairBotVerifyInfo.BasicBotVerifyInfo(qq, password);
		CompletableFuture<Bot> future = CompletableFuture.supplyAsync(() -> botRegistrar.registerBot(basicBotVerifyInfo));
		FileOutputStream fos = null;
		try {
			Bot bot = future.get(30, TimeUnit.SECONDS);
			fos = new FileOutputStream("simbot-bots" + File.separator + qq + ".bot");
			Properties properties = new Properties();
			properties.setProperty("code", qq);
			properties.setProperty("verification", password);
			properties.store(fos, "qq bot properties");
			return Result.success();
		} catch (Exception e) {
			return Result.failure("登录失败，错误信息：" + e.getMessage());
		}finally {
			IOUtils.close(fos);
		}
	}

	@PostMapping("/sendPrivateMsg")
	@ResponseBody
	public Result<?> sendPrivateMsg(long qq, long toQq, String catCode){
		Bot bot = botManager.getBotOrNull(String.valueOf(qq));
		if (bot == null) return Result.failure("该bot不存在，请重试！");
		try {
			bot.getSender().SENDER.sendPrivateMsg(toQq, catCode);
			return Result.success("应该是发送成功了！", null);
		} catch (Exception e) {
			return Result.failure("发送失败，异常信息：" + e.getMessage());
		}
	}


}
