package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.jpa.JPADao;
import me.kuku.yuq.entity.QQBindEntity;

public interface QQBindDao extends JPADao<QQBindEntity, Integer> {
	QQBindEntity findByQQ(long qq);
}
