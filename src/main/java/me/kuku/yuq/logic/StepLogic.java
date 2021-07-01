package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.StepEntity;

import java.io.IOException;

@AutoBind
public interface StepLogic {
	Result<StepEntity> login(String phone, String password) throws IOException;
	Result<String> modifyStepCount(StepEntity stepEntity, int step) throws IOException;
}
