package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.OfficeCodeDao;
import me.kuku.yuq.entity.OfficeCodeEntity;
import me.kuku.yuq.service.OfficeCodeService;

import javax.inject.Inject;
import java.util.List;

public class OfficeCodeServiceImpl implements OfficeCodeService {
	@Inject
	private OfficeCodeDao officeCodeDao;

	@Override
	public OfficeCodeEntity findByCode(String code) {
		return officeCodeDao.findByCode(code);
	}

	@Override
	public void save(OfficeCodeEntity entity) {
		officeCodeDao.saveOrUpdate(entity);
	}

	@Override
	public List<OfficeCodeEntity> findAll() {
		return officeCodeDao.findAll();
	}

	@Override
	@Transactional
	public void delByCode(String code) {
		officeCodeDao.delByCode(code);
	}
}
