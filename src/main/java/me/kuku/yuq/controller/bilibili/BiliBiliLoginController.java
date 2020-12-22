package me.kuku.yuq.controller.bilibili;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.entity.Group;
import me.kuku.yuq.entity.BiliBiliEntity;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.BiliBiliService;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@GroupController
@SuppressWarnings("unused")
public class BiliBiliLoginController {
    @Inject
    private BiliBiliLogic biliBiliLogic;
    @Inject
    private BiliBiliService biliBiliService;
    @Inject
    private ToolLogic toolLogic;

    @Action("bllogin qr")
    @Synonym({"bilibililogin qr"})
    public void biliBiliLoginByQr(Group group, Long qq) throws IOException {
        String url = biliBiliLogic.loginByQr1();
        byte[] qrUrl = toolLogic.creatQr(url);
        group.sendMessage(FunKt.getMif().at(qq).plus("请使用哔哩哔哩APP扫码登录：")
                .plus(FunKt.getMif().imageByInputStream(new ByteArrayInputStream(qrUrl))));
        AtomicInteger i = new AtomicInteger();
        new Thread(() -> {
            while (true){
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i.incrementAndGet() >= 20){
                    group.sendMessage(FunKt.getMif().at(qq).plus("您的二维码已失效！！"));
                    break;
                }
                try {
                    Result<BiliBiliEntity> result = biliBiliLogic.loginByQr2(url);
                    switch (result.getCode()){
                        case 500:
                            group.sendMessage(FunKt.getMif().at(qq).plus(result.getMessage()));
                            return;
                        case 200:
                            BiliBiliEntity biliBiliEntity = biliBiliService.findByQQ(qq);
                            if (biliBiliEntity == null) biliBiliEntity = new BiliBiliEntity(qq, group.getId());
                            BiliBiliEntity newBiliBiliEntity = result.getData();
                            biliBiliEntity.setCookie(newBiliBiliEntity.getCookie());
                            biliBiliEntity.setToken(newBiliBiliEntity.getToken());
                            biliBiliEntity.setUserId(newBiliBiliEntity.getUserId());
                            biliBiliService.save(biliBiliEntity);
                            group.sendMessage(FunKt.getMif().at(qq).plus("绑定或者更新哔哩哔哩成功！！"));
                            return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}
