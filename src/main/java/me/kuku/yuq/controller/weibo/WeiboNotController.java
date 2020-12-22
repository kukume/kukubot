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
@SuppressWarnings("unused")
public class WeiboNotController {
    @Inject
    private WeiboLogic weiboLogic;

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
