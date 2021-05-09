package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.jpa.JPADao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import me.kuku.yuq.entity.OfficeCodeEntity;

public interface OfficeCodeDao extends JPADao<OfficeCodeEntity, Integer> {
	OfficeCodeEntity findByCode(String code);
	@Execute("delete from OfficeCodeEntity where code = ?0")
	void delByCode(String code);
}
