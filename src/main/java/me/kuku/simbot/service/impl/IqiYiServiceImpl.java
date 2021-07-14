package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.IqiYiEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.repository.IqiYiRepository;
import me.kuku.simbot.service.IqiYiService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class IqiYiServiceImpl implements IqiYiService {
	@Resource
	private IqiYiRepository iqiYiRepository;
	@Override
	public IqiYiEntity findByQqEntity(QqEntity qqEntity) {
		return iqiYiRepository.findByQqEntity(qqEntity);
	}

	@Override
	public IqiYiEntity save(IqiYiEntity entity) {
		return iqiYiRepository.save(entity);
	}

	@Override
	public List<IqiYiEntity> findAll() {
		return iqiYiRepository.findAll();
	}
}
