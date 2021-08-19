package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.pojo.QqLoginQrcode;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.HeyTapEntity;
import me.kuku.yuq.pojo.HeyTapQrcode;

import java.io.IOException;

@SuppressWarnings("UnusedReturnValue")
@AutoBind
public interface HeyTapLogic {
	HeyTapQrcode getQrcode() throws IOException;
	Result<HeyTapEntity> checkQrcode(HeyTapQrcode heyTapQrcode) throws IOException;
	QqLoginQrcode getQqQrcode() throws IOException;
	Result<HeyTapEntity> checkQqQrcode(QqLoginQrcode qqLoginQrcode) throws IOException;
	Result<Void> sign(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> viewGoods(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> shareGoods(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> viewPush(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> earlyBedRegistration(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> pointsEveryDay(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> transferPoints(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> smartLifeLottery(HeyTapEntity heyTapEntity) throws IOException;
	Result<Void> petFanPlan(HeyTapEntity heyTapEntity) throws IOException;
}
