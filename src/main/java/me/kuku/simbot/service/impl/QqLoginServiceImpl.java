package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.simbot.repository.QqLoginRepository;
import me.kuku.simbot.service.QqLoginService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class QqLoginServiceImpl implements QqLoginService {

	@Resource
	private QqLoginRepository qqLoginRepository;

	@Override
	public QqLoginEntity findByQqEntity(QqEntity qqEntity) {
		return qqLoginRepository.findByQqEntity(qqEntity);
	}

	@Override
	public List<QqLoginEntity> findAll() {
		return qqLoginRepository.findAll();
	}

	@Override
	public QqLoginEntity save(QqLoginEntity entity) {
		return qqLoginRepository.save(entity);
	}
}
