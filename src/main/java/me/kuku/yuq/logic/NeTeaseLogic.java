package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.NeTeaseEntity;

import java.io.IOException;

@AutoBind
public interface NeTeaseLogic {
    Result<NeTeaseEntity> loginByPhone(String username, String password) throws IOException;
    String sign(NeTeaseEntity neTeaseEntity) throws IOException;
    String listeningVolume(NeTeaseEntity neTeaseEntity) throws IOException;
}
