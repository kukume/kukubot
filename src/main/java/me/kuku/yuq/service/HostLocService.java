package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.HostLocEntity;

import java.util.List;

@AutoBind
public interface HostLocService {
    List<HostLocEntity> findAll();
    HostLocEntity findByQQ(long qq);
    void save(HostLocEntity hostLocEntity);
}
