package me.kuku.yuq.controller.arknights;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.service.ArkNightsTagService;

import javax.inject.Inject;
import java.util.List;

/**
 * 明日方舟ArkNights 公招查询
 */
@GroupController
public class ArkNightsTagController {
    @Inject
    private ArkNightsTagService arkNightsTagService;

    @Action("ark公招查询")
    @QMsg(at = true, atNewLine = true)
    public String arkTagInfo(Message message) {
        List<String> tags = message.toPath();
        tags.remove(0);
        return arkNightsTagService.arkTagInfo(tags);
    }

}
