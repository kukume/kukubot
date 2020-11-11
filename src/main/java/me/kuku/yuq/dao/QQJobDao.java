package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import me.kuku.yuq.entity.QQJobEntity;

import java.util.List;

@Dao
public interface QQJobDao extends YuDao<QQJobEntity, Integer> {
    QQJobEntity findByQQAndType(Long qq, String type);
    List<QQJobEntity> findByQQ(Long qq);
    List<QQJobEntity> findByType(String type);
    @Execute("delete from QQJobEntity where qq = ?0")
    void delByQQ(Long qq);
}