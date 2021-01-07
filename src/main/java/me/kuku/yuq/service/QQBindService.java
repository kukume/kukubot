package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.QQBindEntity;

@AutoBind
public interface QQBindService {
	QQBindEntity findByQQ(long qq);
	void save(QQBindEntity qqBindEntity);
}
