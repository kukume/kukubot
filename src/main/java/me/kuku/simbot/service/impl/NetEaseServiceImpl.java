package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.NetEaseEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.repository.NetEaseRepository;
import me.kuku.simbot.service.NetEaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NetEaseServiceImpl implements NetEaseService {

	@Autowired
	private NetEaseRepository netEaseRepository;

	@Override
	public NetEaseEntity findByQqEntity(QqEntity qqEntity) {
		return netEaseRepository.findByQqEntity(qqEntity);
	}

	@Override
	public List<NetEaseEntity> findAll() {
		return netEaseRepository.findAll();
	}

	@Override
	public NetEaseEntity save(NetEaseEntity entity) {
		return netEaseRepository.save(entity);
	}
}
