package me.kuku.simbot.config;

import love.forte.simbot.bot.BotVerifyInfo;
import love.forte.simbot.component.mirai.MiraiBotConfigurationFactory;
import love.forte.simbot.component.mirai.configuration.MiraiConfiguration;
import net.mamoe.mirai.utils.BotConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class MiraiBot implements MiraiBotConfigurationFactory {
	@NotNull
	@Override
	public BotConfiguration getMiraiBotConfiguration(@NotNull BotVerifyInfo botInfo, @NotNull MiraiConfiguration simbotMiraiConfig) {
		BotConfiguration botConfiguration = simbotMiraiConfig.getBotConfiguration().invoke(botInfo.get("code"));
		System.setProperty("mirai.slider.captcha.supported", "true");
		botConfiguration.fileBasedDeviceInfo();
		return botConfiguration;
	}
}
