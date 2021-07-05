package me.kuku.simbot.service;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.StepEntity;

public interface StepService {
	StepEntity findByQqEntity(QqEntity qqEntity);
	StepEntity save(StepEntity stepEntity);
}
