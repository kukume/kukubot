package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.repository.QqRepository;
import me.kuku.simbot.service.QqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QqServiceImpl implements QqService {
	@Autowired
	private QqRepository qqRepository;

	@Override
	public QqEntity findByQq(Long qq) {
		return qqRepository.findByQq(qq);
	}

	@Override
	public QqEntity save(QqEntity entity) {
		return qqRepository.save(entity);
	}
}
