package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;

@AutoBind
public interface IdentifyCodeLogic {
	Result<String> identify(String type, byte[] bytes) throws IOException;
}
