package me.kuku.yuq.controller.manage;

import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Cache;
import com.IceCreamQAQ.Yu.annotation.Global;
import com.IceCreamQAQ.Yu.cache.EhcacheHelp;
import com.IceCreamQAQ.Yu.entity.DoNone;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.service.GroupService;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;

@GroupController
@SuppressWarnings("unused")
public class BeforeController {
    @Inject
    @Named("CommandCountOnTime")
    private EhcacheHelp<Integer> eh;
    @Inject
    private GroupService groupService;

    @Global
    @Before
    public void before(Message message, Long group, Long qq){
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) return;
        Integer maxCount = groupEntity.getMaxCommandCountOnTime();
        if (maxCount == null) maxCount = -1;
        if (maxCount < 0) return;
        List<String> list = message.toPath();
        if (list.size() == 0) return;
        String command = list.get(0);
        String key = qq.toString() + command;
        Integer num = eh.get(key);
        if (num == null) num = 0;
        if (num >= maxCount) throw new DoNone();
        eh.set(key, ++num);
    }

    @Global
    @Cache
    public void interIO(IOException e, long qq){
        FunKt.getMif().at(qq).plus("出现io异常了，请重试！！");
    }
}
