package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqVideoEntity;
import me.kuku.simbot.repository.QqVideoRepository;
import me.kuku.simbot.service.QqVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QqVideoServiceImpl implements QqVideoService {

	@Autowired
	private QqVideoRepository qqVideoRepository;

	@Override
	public QqVideoEntity findByQqEntity(QqEntity qqEntity) {
		return qqVideoRepository.findByQqEntity(qqEntity);
	}

	@Override
	public List<QqVideoEntity> findAll() {
		return qqVideoRepository.findAll();
	}

	@Override
	public QqVideoEntity save(QqVideoEntity entity) {
		return qqVideoRepository.save(entity);
	}
}
