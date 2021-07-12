package me.kuku.simbot.service;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.WeiboEntity;

import java.util.List;

public interface WeiboService {
	WeiboEntity save(WeiboEntity weiboEntity);
	WeiboEntity findByQqEntity(QqEntity qqEntity);
	List<WeiboEntity> findByMonitor(Boolean monitor);
}
