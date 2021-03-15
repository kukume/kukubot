package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.NeTeaseEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;

@AutoBind
public interface NeTeaseLogic {
    Result<NeTeaseEntity> loginByPhone(String username, String password) throws IOException;
    Result<NeTeaseEntity> loginByQQ(QQLoginEntity qqLoginEntity) throws IOException;
    String sign(NeTeaseEntity neTeaseEntity) throws IOException;
    String listeningVolume(NeTeaseEntity neTeaseEntity) throws IOException;
}
