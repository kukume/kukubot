package me.kuku.simbot.interceptor;

import me.kuku.simbot.entity.NetEaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class NetEaseInterceptor extends CheckExistInterceptor<NetEaseService>{
	@NotNull
	@Override
	protected String[] getGroupRange() {
		return new String[]{"netEase"};
	}

	@Override
	protected String notExistMsg() {
		return "您还没有绑定网易账号，请发送<网易二维码>进行绑定！";
	}
}
