package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.pojo.ConfigType;

import java.util.List;

@AutoBind
public interface ConfigService {
    List<ConfigEntity> findAll();
    void save(ConfigEntity configEntity);
    ConfigEntity findByType(String type);
    ConfigEntity findByType(ConfigType configType);
}
