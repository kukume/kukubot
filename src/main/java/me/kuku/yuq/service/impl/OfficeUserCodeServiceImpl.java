package me.kuku.yuq.service.impl;

import me.kuku.yuq.dao.OfficeUserCodeDao;
import me.kuku.yuq.entity.OfficeUserCodeEntity;
import me.kuku.yuq.service.OfficeUserCodeService;

import javax.inject.Inject;

public class OfficeUserCodeServiceImpl implements OfficeUserCodeService {
	@Inject
	private OfficeUserCodeDao officeUserCodeDao;

	@Override
	public OfficeUserCodeEntity findByCode(String code) {
		return officeUserCodeDao.findByCode(code);
	}

	@Override
	public void save(OfficeUserCodeEntity entity) {
		officeUserCodeDao.saveOrUpdate(entity);
	}
}
