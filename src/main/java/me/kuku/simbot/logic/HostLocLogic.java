package me.kuku.simbot.logic;

import me.kuku.pojo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface HostLocLogic {
	Result<String> login(String username, String password) throws IOException;
	boolean isLogin(String cookie) throws IOException;
	void sign(String cookie) throws IOException;
	List<Map<String, String>> post();
}
