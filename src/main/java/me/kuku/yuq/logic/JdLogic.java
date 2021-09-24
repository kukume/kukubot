package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.QlEntity;
import me.kuku.yuq.pojo.JdQrcode;

import java.io.IOException;

@AutoBind
public interface JdLogic {
    JdQrcode qrcode() throws IOException;
    Result<String> cookie(JdQrcode jdQrcode) throws IOException;
    Result<?> qlCookie(QlEntity qlEntity, String cookie) throws IOException;
}
