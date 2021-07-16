package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.pojo.KeyTapQrcode;

import java.io.IOException;

public interface KeyTapLogic {
	KeyTapQrcode getQrcode() throws IOException;
	Result<?> checkQrcode(KeyTapQrcode keyTapQrcode) throws IOException;
}
