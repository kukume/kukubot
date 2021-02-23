package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.IceCreamQAQ.Yu.util.IO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.entity.Friend;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JobCenter
@SuppressWarnings("unused")
public class MyJob {
    @Config("YuQ.Mirai.bot.master")
    public String master;
    @Inject
    private GroupService groupService;

    @Cron("1h")
    public void backUp(){
        File confFile = new File("conf");
        if (confFile.exists()){
            String deviceName = "device.json";
            File rootDeviceFile = new File(deviceName);
            File confDeviceFile = new File("conf/" + deviceName);
            if (rootDeviceFile.exists()){
                try {
                    IO.writeFile(confDeviceFile, IO.read(new FileInputStream(rootDeviceFile), true));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
