package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.HostLocEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.repository.HostLocRepository;
import me.kuku.simbot.service.HostLocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostLocServiceImpl implements HostLocService {

	@Autowired
	private HostLocRepository hostLocRepository;

	@Override
	public HostLocEntity findByQqEntity(QqEntity qqEntity) {
		return hostLocRepository.findByQqEntity(qqEntity);
	}

	@Override
	public HostLocEntity save(HostLocEntity entity) {
		return hostLocRepository.save(entity);
	}

	@Override
	public List<HostLocEntity> findAll() {
		return hostLocRepository.findAll();
	}
}
