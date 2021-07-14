package me.kuku.simbot;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.IqiYiEntity;
import me.kuku.simbot.logic.impl.IqiYiLogicImpl;
import me.kuku.simbot.pojo.IqiYiQrcode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SomeTest {
	public static void main(String[] args) {
		try {
			IqiYiLogicImpl iqiYiLogic = new IqiYiLogicImpl();
			IqiYiQrcode qrcode = iqiYiLogic.getQrcode();
			System.out.println(qrcode);
			while (true){
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Result<IqiYiEntity> result =
						iqiYiLogic.checkQrcode(qrcode);
				System.out.println(result);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
