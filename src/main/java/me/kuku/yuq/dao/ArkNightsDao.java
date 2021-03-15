package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.jpa.JPADao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import me.kuku.yuq.entity.ArkNightsEntity;

public interface ArkNightsDao extends JPADao<ArkNightsEntity, Integer> {
	ArkNightsEntity findByQQ(long qq);
	@Execute("delete from ArkNightsEntity where qq = ?0")
	int delByQQ(long qq);
}
