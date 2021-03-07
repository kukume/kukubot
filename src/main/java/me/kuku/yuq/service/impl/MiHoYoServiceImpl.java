package me.kuku.yuq.service.impl;

import me.kuku.yuq.dao.MiHoYoDao;
import me.kuku.yuq.entity.MiHoYoEntity;
import me.kuku.yuq.service.MiHoYoService;

import javax.inject.Inject;
import java.util.List;

public class MiHoYoServiceImpl implements MiHoYoService {
	@Inject
	private MiHoYoDao miHoYoDao;
	@Override
	public MiHoYoEntity findByQQ(long qq) {
		return miHoYoDao.findByQQ(qq);
	}

	@Override
	public void save(MiHoYoEntity miHoYoEntity) {
		miHoYoDao.saveOrUpdate(miHoYoEntity);
	}

	@Override
	public int delByQQ(long qq) {
		return miHoYoDao.delByQQ(qq);
	}

	@Override
	public List<MiHoYoEntity> findAll() {
		return miHoYoDao.findAll();
	}
}
