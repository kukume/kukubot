package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.BiliBiliEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.repository.BiliBiliRepository;
import me.kuku.simbot.service.BiliBiliService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BiliBiliServiceImpl implements BiliBiliService {

	@Autowired
	private BiliBiliRepository biliBiliRepository;

	@Override
	public BiliBiliEntity findByQqEntity(QqEntity qqEntity) {
		return biliBiliRepository.findByQqEntity(qqEntity);
	}

	@Override
	public BiliBiliEntity save(BiliBiliEntity entity) {
		return biliBiliRepository.save(entity);
	}

	@Override
	public List<BiliBiliEntity> findAll() {
		return biliBiliRepository.findAll();
	}

	@Override
	public List<BiliBiliEntity> findByMonitor(Boolean monitor) {
		return biliBiliRepository.findByMonitor(monitor);
	}

	@Override
	public List<BiliBiliEntity> findByTask(Boolean task) {
		return biliBiliRepository.findByTask(task);
	}

	@Override
	public List<BiliBiliEntity> findByLive(Boolean live) {
		return biliBiliRepository.findByLive(live);
	}
}
