package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.StepEntity;

import java.io.IOException;

public interface StepLogic {
	Result<StepEntity> login(String phone, String password) throws IOException;
	Result<String> modifyStepCount(StepEntity stepEntity, int step) throws IOException;
}
