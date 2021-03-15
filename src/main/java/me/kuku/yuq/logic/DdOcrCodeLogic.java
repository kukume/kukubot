package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.DdOcrPojo;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;

@AutoBind
public interface DdOcrCodeLogic {

	Result<DdOcrPojo> identify(String gt, String challenge, String referer) throws IOException;
}
