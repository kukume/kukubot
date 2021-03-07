package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.MiHoYoEntity;

import java.util.List;

@AutoBind
public interface MiHoYoService {
	MiHoYoEntity findByQQ(long qq);
	void save(MiHoYoEntity miHoYoEntity);
	int delByQQ(long qq);
	List<MiHoYoEntity> findAll();
}
