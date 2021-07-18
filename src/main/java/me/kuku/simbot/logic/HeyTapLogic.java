package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.HeyTapEntity;
import me.kuku.simbot.pojo.HeyTapQrcode;

import java.io.IOException;

public interface HeyTapLogic {
	HeyTapQrcode getQrcode() throws IOException;
	Result<HeyTapEntity> checkQrcode(HeyTapQrcode heyTapQrcode) throws IOException;
	Result<Void> sign(HeyTapEntity heyTapEntity) throws IOException;
}
