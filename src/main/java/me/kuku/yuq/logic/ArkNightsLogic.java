package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.ArkNightsEntity;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@AutoBind
public interface ArkNightsLogic {
	Result<ArkNightsEntity> login(String account, String password) throws IOException;
	Result<List<Map<String, String>>> rechargeRecord(String cookie) throws IOException;
	Result<List<Map<String, String>>> searchRecord(String cookie, Integer page) throws IOException;
	Result<List<Map<String, String>>> sourceRecord(String cookie, Integer page) throws IOException;
}
