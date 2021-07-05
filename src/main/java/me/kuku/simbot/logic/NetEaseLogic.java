package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.NetEaseEntity;

import java.io.IOException;

public interface NetEaseLogic {
	Result<NetEaseEntity> loginByPhone(String phone, String password) throws IOException;
	Result<NetEaseEntity> loginByEmail(String email, String password) throws IOException;
	Result<?> sign(NetEaseEntity netEaseEntity) throws IOException;
	Result<?> listeningVolume(NetEaseEntity netEaseEntity) throws IOException;
}
