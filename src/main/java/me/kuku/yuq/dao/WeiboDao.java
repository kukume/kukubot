package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import com.icecreamqaq.yudb.jpa.annotation.Execute;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.WeiboEntity;

import java.util.List;

@Dao
public interface WeiboDao extends YuDao<WeiboEntity, Integer> {
    WeiboEntity findByQQ(Long qq);
    @Execute("delete from WeiboEntity where qq = ?0")
    int delByQQ(Long qq);
    List<WeiboEntity> findByMonitor(boolean monitor);
    @Select("from WeiboEntity")
    List<WeiboEntity> findAll();
}