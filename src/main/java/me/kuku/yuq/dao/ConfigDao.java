package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.ConfigEntity;

import java.util.List;

@Dao
public interface ConfigDao extends YuDao<ConfigEntity, Integer> {
    @Select("from ConfigEntity")
    List<ConfigEntity> findAll();
    ConfigEntity findByType(String type);
}