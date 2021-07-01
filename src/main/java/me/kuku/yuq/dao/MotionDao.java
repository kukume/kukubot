package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.StepEntity;

import java.util.List;

@Dao
public interface MotionDao extends YuDao<StepEntity, Integer> {
    StepEntity findByQQ(Long qq);
    @Select("from MotionEntity")
    List<StepEntity> findAll();
    @Execute("delete from MotionEntity where qq = ?0")
    void delByQQ(Long qq);
}