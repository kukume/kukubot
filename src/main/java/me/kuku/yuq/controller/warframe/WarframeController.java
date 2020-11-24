package me.kuku.yuq.controller.warframe;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.annotation.QMsg;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.utils.OkHttpUtils;

import java.io.IOException;

/**
 * FileName: WarframeController
 * Author:   wsure
 * Date:     2020/11/24 5:33 下午
 * Description: warframe 查询插件
 * 单独的Mirai-console插件可以查看：
 *  https://github.com/WsureDev/warframe-world-state/releases
 */

@GroupController
@PrivateController
public class WarframeController {

    @Config("warframe.info.api.host")
    private String API_HOST;

    @QMsg(at = true)
    @Action("wiki {content}")
    public String wfWiki(String content) throws IOException {
        return OkHttpUtils.getStr(String.format("%s/wiki/robot/%s",API_HOST,content));
    }

    @QMsg(at = true)
    @Action("rm {content}")
    public String rivenMarket(String content) throws IOException {
        return OkHttpUtils.getStr(String.format("%s/rm/robot/%s",API_HOST,content));
    }

    @QMsg(at = true)
    @Action("wm {content}")
    public String warframeMarket(String content) throws IOException {
        return OkHttpUtils.getStr(String.format("%s/wm/robot/%s",API_HOST,content));
    }

    @Action("新闻")
    @Synonym({"事件",
            "警报",
            "突击",
            "地球赏金",
            "金星赏金",
            "火卫二赏金",
            "裂缝",
            "促销商品",
            "入侵",
            "奸商",
            "特价",
            "小小黑",
            "地球",
            "地球平原",
            "火卫二平原",
            "金星平原",
            "电波",
            "仲裁"})
    @QMsg(at = true, atNewLine = true)
    public String warframeWorldState(GroupEntity groupEntity, @PathVar(0) String type) throws IOException {
        String name = WorldStateKey.getNameByKeyWord(type);
        return OkHttpUtils.getStr(String.format("%s/wf/robot/%s",API_HOST,name));
    }

}
