package me.kuku.simbot.interceptor;

import me.kuku.simbot.entity.HeyTapService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class HeyTapInterceptor extends CheckExistInterceptor<HeyTapService> {
	@Override
	protected String notExistMsg() {
		return "您还没有绑定欢太账号，请发送<欢太二维码>进行绑定！";
	}

	@NotNull
	@Override
	protected String[] getGroupRange() {
		return new String[]{"heyTap"};
	}
}
