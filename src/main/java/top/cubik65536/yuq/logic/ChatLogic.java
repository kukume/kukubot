package top.cubik65536.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;

/**
 * ChatLogic
 * me.kuku.yuq.logic
 * kukubot
 * <p>
 * Created by Cubik65536 on 2021-02-20.
 * Copyright © 2020-2021 Cubik Inc. All rights reserved.
 * <p>
 * Description: QQ智能聊天机器人API接口
 * History:
 * 1. 2021-02-20 [Cubik65536]: Create file ChatLogic;
 * 2. 2021-02-20 [Cubik65536]: 声明青云客和海知智能聊天机器人方法;
 */

@AutoBind
public interface ChatLogic {
    // 青云客API
    String getQingYunKe(String key, String msg) throws Exception;
    // 海知智能API
    String getHaiZhi(String key, String msg) throws Exception;
}
