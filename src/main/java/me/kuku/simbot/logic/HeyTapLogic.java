package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.HeyTapEntity;
import me.kuku.simbot.pojo.HeyTapQrcode;

import java.io.IOException;

@SuppressWarnings("UnusedReturnValue")
public interface HeyTapLogic {
	HeyTapQrcode getQrcode() throws IOException;
	Result<HeyTapEntity> checkQrcode(HeyTapQrcode heyTapQrcode) throws IOException;
	Result<Void> sign(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> viewGoods(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> shareGoods(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> viewPush(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> earlyBedRegistration(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> earlyBedPunchCard(HeyTapEntity heyTapEntity);
	Result<Void> pointsEveryDay(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> transferPoints(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> smartLifeLottery(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> petFanPlan(HeyTapEntity heyTapEntity) throws IOException;
}
