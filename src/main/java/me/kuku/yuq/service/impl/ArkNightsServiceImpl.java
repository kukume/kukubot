package me.kuku.yuq.service.impl;

import me.kuku.yuq.dao.ArkNightsDao;
import me.kuku.yuq.entity.ArkNightsEntity;
import me.kuku.yuq.service.ArkNightsService;

import javax.inject.Inject;
import java.util.List;

public class ArkNightsServiceImpl implements ArkNightsService {
	@Inject
	private ArkNightsDao arkNightsDao;

	@Override
	public ArkNightsEntity findByQQ(long qq) {
		return arkNightsDao.findByQQ(qq);
	}

	@Override
	public int delByQQ(long qq) {
		return arkNightsDao.delByQQ(qq);
	}

	@Override
	public void save(ArkNightsEntity arkNightsEntity) {
		arkNightsDao.saveOrUpdate(arkNightsEntity);
	}

	@Override
	public List<ArkNightsEntity> findAll() {
		return arkNightsDao.findAll();
	}
}
