package me.kuku.simbot.interceptor;

import me.kuku.simbot.entity.QqLoginService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class QqLoginInterceptor extends CheckExistInterceptor<QqLoginService> {

	@Override
	protected String notExistMsg() {
		return "您还没有绑定qq信息，请发送<qq二维码>进行绑定";
	}

	@NotNull
	@Override
	protected String[] getGroupRange() {
		return new String[]{"qqLogin"};
	}
}
