package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.OfficeUserCodeEntity;

@AutoBind
public interface OfficeUserCodeService {
	OfficeUserCodeEntity findByCode(String code);
	void save(OfficeUserCodeEntity entity);
}
