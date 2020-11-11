package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import me.kuku.yuq.entity.RecallEntity;

import java.util.List;

@Dao
public interface RecallDao extends YuDao<RecallEntity, Integer> {
    List<RecallEntity> findByGroupAndQQ(Long group, Long qq);
}