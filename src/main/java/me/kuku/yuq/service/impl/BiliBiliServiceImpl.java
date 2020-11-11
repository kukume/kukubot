package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.BiliBiliDao;
import me.kuku.yuq.entity.BiliBiliEntity;
import me.kuku.yuq.service.BiliBiliService;

import javax.inject.Inject;
import java.util.List;

public class BiliBiliServiceImpl implements BiliBiliService {

    @Inject
    private BiliBiliDao biliBiliDao;

    @Override
    @Transactional
    public BiliBiliEntity findByQQ(Long qq) {
        return biliBiliDao.findByQQ(qq);
    }

    @Override
    @Transactional
    public void save(BiliBiliEntity biliBiliEntity) {
        biliBiliDao.saveOrUpdate(biliBiliEntity);
    }

    @Override
    @Transactional
    public int delByQQ(Long qq) {
        return biliBiliDao.delByQQ(qq);
    }

    @Override
    @Transactional
    public List<BiliBiliEntity> findByMonitor(Boolean monitor) {
        return biliBiliDao.findByMonitor(monitor);
    }

    @Override
    @Transactional
    public List<BiliBiliEntity> findAll() {
        return biliBiliDao.findAll();
    }

    @Override
    @Transactional
    public List<BiliBiliEntity> findByTask(Boolean task) {
        return biliBiliDao.findByTask(task);
    }
}
