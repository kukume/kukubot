package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.NetEaseEntity;
import me.kuku.yuq.pojo.NetEaseQrcode;

import java.io.IOException;

@SuppressWarnings("UnusedReturnValue")
@AutoBind
public interface NetEaseLogic {
	Result<NetEaseEntity> loginByPhone(String phone, String password) throws IOException;
	Result<NetEaseEntity> loginByEmail(String email, String password) throws IOException;
	NetEaseQrcode loginByQrcode() throws IOException;
	Result<NetEaseEntity> checkQrcode(NetEaseQrcode netEaseQrcode) throws IOException;
	Result<?> sign(NetEaseEntity netEaseEntity) throws IOException;
	Result<?> listeningVolume(NetEaseEntity netEaseEntity) throws IOException;
	Result<Void> musicianSign(NetEaseEntity netEaseEntity) throws IOException;
}
