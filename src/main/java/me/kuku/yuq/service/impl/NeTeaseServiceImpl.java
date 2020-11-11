package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.NeTeaseDao;
import me.kuku.yuq.entity.NeTeaseEntity;
import me.kuku.yuq.service.NeTeaseService;

import javax.inject.Inject;
import java.util.List;

public class NeTeaseServiceImpl implements NeTeaseService {

    @Inject
    private NeTeaseDao neTeaseDao;

    @Override
    @Transactional
    public NeTeaseEntity findByQQ(Long qq) {
        return neTeaseDao.findByQQ(qq);
    }

    @Override
    @Transactional
    public void save(NeTeaseEntity neTeaseEntity) {
        neTeaseDao.saveOrUpdate(neTeaseEntity);
    }

    @Override
    @Transactional
    public List<NeTeaseEntity> findAll() {
        return neTeaseDao.findAll();
    }

    @Override
    @Transactional
    public int deByQQ(Long qq) {
        return neTeaseDao.delByQQ(qq);
    }
}
