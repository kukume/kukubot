package me.kuku.yuq.service.impl;

import me.kuku.yuq.dao.QQBindDao;
import me.kuku.yuq.entity.QQBindEntity;
import me.kuku.yuq.service.QQBindService;

import javax.inject.Inject;

public class QQBindServiceImpl implements QQBindService {
	@Inject
	private QQBindDao qqBindDao;
	@Override
	public QQBindEntity findByQQ(long qq) {
		return qqBindDao.findByQQ(qq);
	}

	@Override
	public void save(QQBindEntity qqBindEntity) {
		qqBindDao.saveOrUpdate(qqBindEntity);
	}
}
