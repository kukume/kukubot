package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.OfficeCodeEntity;

import java.util.List;

@AutoBind
public interface OfficeCodeService {
	OfficeCodeEntity findByCode(String code);
	void save(OfficeCodeEntity entity);
	List<OfficeCodeEntity> findAll();
	void delByCode(String code);
}
