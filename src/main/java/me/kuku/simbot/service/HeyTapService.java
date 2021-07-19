package me.kuku.simbot.service;

import me.kuku.simbot.entity.HeyTapEntity;
import me.kuku.simbot.entity.QqEntity;

import java.util.List;

public interface HeyTapService {
	HeyTapEntity findByQqEntity(QqEntity qqEntity);
	HeyTapEntity save(HeyTapEntity entity);
	List<HeyTapEntity> findAll();
	void delete(HeyTapEntity heyTapEntity);
}
