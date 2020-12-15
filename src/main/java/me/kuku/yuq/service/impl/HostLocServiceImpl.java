package me.kuku.yuq.service.impl;

import me.kuku.yuq.dao.HostLocDao;
import me.kuku.yuq.entity.HostLocEntity;
import me.kuku.yuq.service.HostLocService;

import javax.inject.Inject;
import java.util.List;

public class HostLocServiceImpl implements HostLocService {

    @Inject
    private HostLocDao hostLocDao;

    @Override
    public List<HostLocEntity> findAll() {
        return hostLocDao.findAll();
    }

    @Override
    public HostLocEntity findByQQ(long qq) {
        return hostLocDao.findByQQ(qq);
    }

    @Override
    public void save(HostLocEntity hostLocEntity) {
        hostLocDao.saveOrUpdate(hostLocEntity);
    }
}
