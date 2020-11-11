package me.kuku.yuq.controller.weibo;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Group;
import me.kuku.yuq.entity.WeiboEntity;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.WeiboPojo;
import me.kuku.yuq.service.WeiboService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@GroupController
public class WeiboNotController {
    @Inject
    private WeiboLogic weiboLogic;
    @Inject
    private WeiboService weiboService;


    @Action("wblogin")
    public void wbLoginByQr(long qq, Group group) throws IOException {
        Map<String, String> map = weiboLogic.loginByQr1();
        group.sendMessage(FunKt.getMif().at(qq).plus("请使用微博APP扫码登录").plus(
                FunKt.getMif().imageByUrl("https:" + map.get("url"))
        ));
        new Thread(() -> {
            String id = map.get("id");
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Result<WeiboEntity> result = weiboLogic.loginByQr2(id);
                    if (result.getCode() == 200) {
                        WeiboEntity newWeiboEntity = result.getData();
                        WeiboEntity weiboEntity = weiboService.findByQQ(qq);
                        if (weiboEntity == null) weiboEntity = new WeiboEntity(qq, group.getId());
                        weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
                        weiboEntity.setGroup(group.getId());
                        weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
                        weiboService.save(weiboEntity);
                        group.sendMessage(FunKt.getMif().at(qq).plus("绑定微博信息成功！！"));
                        break;
                    }else if (result.getCode() == 500){
                        group.sendMessage(FunKt.getMif().at(qq).plus(result.getMessage()));
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Action("热搜")
    @QMsg(at = true, atNewLine = true)
    public String hotSearch() throws IOException {
        List<String> list = weiboLogic.hotSearch();
        StringBuilder sb = new StringBuilder();
        for (String str: list){
            sb.append(str).append("\n");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Action("wbinfo {username}")
    @QMsg(at = true, atNewLine = true)
    public String weiboInfo(String username) throws IOException {
        Result<List<WeiboPojo>> idResult = weiboLogic.getIdByName(username);
        List<WeiboPojo> idList = idResult.getData();
        if (idList == null) return idResult.getMessage();
        return weiboLogic.getUserInfo(idList.get(0).getUserId());
    }
}
