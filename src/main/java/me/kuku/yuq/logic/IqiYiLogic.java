package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.IqiYiEntity;
import me.kuku.yuq.pojo.IqiYiQrcode;

import java.io.IOException;

@AutoBind
public interface IqiYiLogic {
	// IqiYi
	IqiYiQrcode getQrcode() throws IOException;
	Result<IqiYiEntity> checkQrcode(IqiYiQrcode iqiYiQrcode) throws IOException;
	Result<Void> sign(IqiYiEntity iqiYiEntity) throws IOException;
	Result<Void> task(IqiYiEntity iqiYiEntity) throws IOException;
	Result<Void> draw(IqiYiEntity iqiYiEntity) throws IOException;
}
