package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.*;
import me.kuku.yuq.service.DaoService;

import javax.inject.Inject;

public class DaoServiceImpl implements DaoService {
    @Inject
    private QQJobDao qqJobDao;
    @Inject
    private QQLoginDao qqLoginDao;
    @Inject
    private MotionDao motionDao;
    @Inject
    private BiliBiliDao biliBiliDao;
    @Inject
    private NeTeaseDao neTeaseDao;
    @Inject
    private WeiboDao weiboDao;

    @Override
    @Transactional
    public void delByQQ(Long qq) {
        qqJobDao.delByQQ(qq);
        qqLoginDao.delByQQ(qq);
        motionDao.delByQQ(qq);
        biliBiliDao.delByQQ(qq);
        neTeaseDao.delByQQ(qq);
        weiboDao.delByQQ(qq);
    }
}
