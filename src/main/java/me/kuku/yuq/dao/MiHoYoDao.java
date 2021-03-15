package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.jpa.JPADao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.MiHoYoEntity;

import java.util.List;

public interface MiHoYoDao extends JPADao<MiHoYoEntity, Integer> {
	MiHoYoEntity findByQQ(long qq);
	@Execute("delete from MiHoYoEntity where qq = ?0")
	int delByQQ(long qq);
	@Select("from MiHoYoEntity")
	List<MiHoYoEntity> findAll();
}
