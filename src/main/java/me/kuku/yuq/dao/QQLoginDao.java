package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.QQLoginEntity;

import java.util.List;

@Dao
public interface QQLoginDao extends YuDao<QQLoginEntity, Integer> {
    QQLoginEntity findByQQ(Long qq);
    @Select("from QQLoginEntity")
    List<QQLoginEntity> findAll();
    @Select("from QQLoginEntity where status = true")
    List<QQLoginEntity> findByActivity();
    @Execute("delete from QQLoginEntity where qq = ?0")
    void delByQQ(Long qq);
}