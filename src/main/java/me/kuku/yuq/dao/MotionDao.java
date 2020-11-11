package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.MotionEntity;

import java.util.List;

@Dao
public interface MotionDao extends YuDao<MotionEntity, Integer> {
    MotionEntity findByQQ(Long qq);
    @Select("from MotionEntity")
    List<MotionEntity> findAll();
    @Execute("delete from MotionEntity where qq = ?0")
    void delByQQ(Long qq);
}