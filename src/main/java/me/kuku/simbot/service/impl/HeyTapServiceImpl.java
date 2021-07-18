package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.HeyTapEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.repository.HeyTapRepository;
import me.kuku.simbot.service.HeyTapService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class HeyTapServiceImpl implements HeyTapService {

	@Resource
	private HeyTapRepository heyTapRepository;

	@Override
	public HeyTapEntity findByQqEntity(QqEntity qqEntity) {
		return heyTapRepository.findByQqEntity(qqEntity);
	}

	@Override
	public HeyTapEntity save(HeyTapEntity entity) {
		return heyTapRepository.save(entity);
	}

	@Override
	public List<HeyTapEntity> findAll() {
		return heyTapRepository.findAll();
	}
}
