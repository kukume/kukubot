package me.kuku.simbot.controller;

import love.forte.simbot.bot.*;
import me.kuku.pojo.Result;
import me.kuku.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
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

	@RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
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

	@GetMapping("/size")
	@ResponseBody
	public Result<?> size(){
		int size = botManager.getBots().size();
		return Result.success("成功", Result.map("size", size));
	}

	@RequestMapping(value = "/sendPrivateMsg", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result<?> sendPrivateMsg(@RequestParam("bot") long botQq, long qq, String catCode){
		Bot bot = botManager.getBotOrNull(String.valueOf(botQq));
		if (bot == null) return Result.failure("该bot不存在，请重试！");
		try {
			bot.getSender().SENDER.sendPrivateMsg(qq, catCode);
			return Result.success("应该是发送成功了！", null);
		} catch (Exception e) {
			return Result.failure("发送失败，异常信息：" + e.getMessage());
		}
	}

	@RequestMapping(value = "/sendGroupMsg", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result<?> sendGroupMsg(long group, @RequestParam("bot") long botQq, String catCode){
		Bot bot = botManager.getBotOrNull(String.valueOf(botQq));
		if (bot == null) return Result.failure("该bot不存在，请重试！");
		try {
			bot.getSender().SENDER.sendGroupMsg(group, catCode);
			return Result.success("应该是发送成功了！", null);
		} catch (Exception e) {
			return Result.failure("发送失败，异常信息：" + e.getMessage());
		}
	}

	@RequestMapping(value = "/sendTempMsg", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result<?> sendTempMsg(long qq, long group, @RequestParam("bot") long botQq, String catCode){
		Bot bot = botManager.getBotOrNull(String.valueOf(botQq));
		if (bot == null) return Result.failure("该bot不存在，请重试！");
		try {
			bot.getSender().SENDER.sendPrivateMsg(qq, group, catCode);
			return Result.success("应该是发送成功了！", null);
		} catch (Exception e) {
			return Result.failure("发送失败，异常信息：" + e.getMessage());
		}
	}

	@RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result<?> logout(String qq){
		Bot bot = botManager.getBotOrNull(qq);
		if (bot != null){
			botDestroyer.destroyBot(qq);
			return Result.success();
		}else return Result.failure("没有找到这个bot");
	}

	@RequestMapping(value = "/reLogin", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result<?> reLogin(long qq){
		String qqStr = String.valueOf(qq);
		Bot bot = botManager.getBotOrNull(qqStr);
		if (bot != null){
			FileInputStream fis = null;
			try {
				fis = new FileInputStream("simbot-bots" + File.separator + qq + ".bot");
				Properties properties = new Properties();
				properties.load(fis);
				Object password = properties.get("verification");
				if (password == null) password = properties.get("password");
				botDestroyer.destroyBot(qqStr);
				PairBotVerifyInfo.BasicBotVerifyInfo basicBotVerifyInfo = new PairBotVerifyInfo.BasicBotVerifyInfo(qqStr, password.toString());
				CompletableFuture<Bot> future = CompletableFuture.supplyAsync(() -> botRegistrar.registerBot(basicBotVerifyInfo));
				Bot loginBot = future.get(30, TimeUnit.SECONDS);
				return Result.success();
			}catch (Exception e){
				e.printStackTrace();
				return Result.failure("登录失败，错误信息：" + e.getMessage());
			}finally {
				IOUtils.close(fis);
			}
		}else return Result.failure("没有找到这个bot");
	}

}
