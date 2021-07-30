package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.NetEaseEntity;
import me.kuku.simbot.pojo.NetEaseQrcode;

import java.io.IOException;

@SuppressWarnings("UnusedReturnValue")
public interface NetEaseLogic {
	Result<NetEaseEntity> loginByPhone(String phone, String password) throws IOException;
	Result<NetEaseEntity> loginByEmail(String email, String password) throws IOException;
	NetEaseQrcode loginByQrcode() throws IOException;
	Result<NetEaseEntity> checkQrcode(NetEaseQrcode netEaseQrcode) throws IOException;
	Result<?> sign(NetEaseEntity netEaseEntity) throws IOException;
	Result<?> listeningVolume(NetEaseEntity netEaseEntity) throws IOException;
	Result<Void> musicianSign(NetEaseEntity netEaseEntity) throws IOException;
}
