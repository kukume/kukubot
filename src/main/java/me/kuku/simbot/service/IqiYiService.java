package me.kuku.simbot.service;

import me.kuku.simbot.entity.IqiYiEntity;
import me.kuku.simbot.entity.QqEntity;

import java.util.List;

public interface IqiYiService {
	IqiYiEntity findByQqEntity(QqEntity qqEntity);
	IqiYiEntity save(IqiYiEntity entity);
	List<IqiYiEntity> findAll();
	void delete(IqiYiEntity entity);
}
