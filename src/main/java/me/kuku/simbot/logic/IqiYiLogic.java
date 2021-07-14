package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.IqiYiEntity;
import me.kuku.simbot.pojo.IqiYiQrcode;

import java.io.IOException;

public interface IqiYiLogic {
	// IqiYi
	IqiYiQrcode getQrcode() throws IOException;
	Result<IqiYiEntity> checkQrcode(IqiYiQrcode iqiYiQrcode) throws IOException;
	Result<Void> sign(IqiYiEntity iqiYiEntity) throws IOException;
	Result<Void> task(IqiYiEntity iqiYiEntity) throws IOException;
	Result<Void> draw(IqiYiEntity iqiYiEntity) throws IOException;
}
