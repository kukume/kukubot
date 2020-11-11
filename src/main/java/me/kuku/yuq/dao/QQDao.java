package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.QQEntity;

import java.util.List;

@Dao
public interface QQDao extends YuDao<QQEntity, Integer> {
    @Select("from QQEntity where qq = ?0 and group_id = ?1")
    QQEntity findByQQAndGroup(Long qq, Integer group);
    @Execute("delete from QQEntity where qq = ?0 and group_id = ?1")
    int delByQQAndGroup(Long qq, Integer group);
    @Select("from QQEntity")
    List<QQEntity> findAll();
}