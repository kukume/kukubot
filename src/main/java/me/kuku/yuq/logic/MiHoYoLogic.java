package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;

@SuppressWarnings("UnusedReturnValue")
@AutoBind
public interface MiHoYoLogic {
	Result<MiHoYoEntity> login(String account, String password) throws IOException;
	String sign(MiHoYoEntity miHoYoEntity) throws IOException;
//	String bbsSign(MiHoYoEntity miHoYoEntity) throws IOException;
//	String bbsPost(MiHoYoEntity miHoYoEntity, String id) throws IOException;
//	String bbsLike(MiHoYoEntity miHoYoEntity, String id) throws IOException;
//	String bbsShare(MiHoYoEntity miHoYoEntity, String id) throws IOException;
	String genShinUserInfo(long id) throws IOException;
}
