package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;

@AutoBind
public interface XiaomiMotionLogic {
    Result<String> login(String phone, String password) throws IOException;
    String changeStep(String token, int step) throws IOException;
}
