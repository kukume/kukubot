package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.jpa.JPADao;
import me.kuku.yuq.entity.OfficeUserCodeEntity;

public interface OfficeUserCodeDao extends JPADao<OfficeUserCodeEntity, Integer> {
	OfficeUserCodeEntity findByCode(String code);
}
