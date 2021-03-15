package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.jpa.JPADao;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.HostLocEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HostLocDao extends JPADao<HostLocEntity, Integer> {
    @NotNull
    @Select("from HostLocEntity")
    List<HostLocEntity> findAll();
    HostLocEntity findByQQ(long qq);
}
