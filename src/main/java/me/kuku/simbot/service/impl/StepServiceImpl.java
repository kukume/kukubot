package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.StepEntity;
import me.kuku.simbot.repository.StepRepository;
import me.kuku.simbot.service.StepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StepServiceImpl implements StepService {

	@Autowired
	private StepRepository stepRepository;

	@Override
	public StepEntity findByQqEntity(QqEntity qqEntity) {
		return stepRepository.findByQqEntity(qqEntity);
	}

	@Override
	public StepEntity save(StepEntity stepEntity) {
		return stepRepository.save(stepEntity);
	}
}
