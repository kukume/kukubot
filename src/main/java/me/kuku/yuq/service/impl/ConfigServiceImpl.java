package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.ConfigDao;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.pojo.ConfigType;
import me.kuku.yuq.service.ConfigService;

import javax.inject.Inject;
import java.util.List;

public class ConfigServiceImpl implements ConfigService {

    @Inject
    private ConfigDao configDao;

    @Override
    @Transactional
    public List<ConfigEntity> findAll() {
        return configDao.findAll();
    }

    @Override
    @Transactional
    public void save(ConfigEntity configEntity) {
        configDao.saveOrUpdate(configEntity);
    }

    @Override
    @Transactional
    public ConfigEntity findByType(String type) {
        return configDao.findByType(type);
    }

    @Override
    public ConfigEntity findByType(ConfigType configType) {
        return findByType(configType.getType());
    }
}
