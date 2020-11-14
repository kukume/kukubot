package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.IceCreamQAQ.Yu.util.IO;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.service.GroupService;
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
public class MyJob {
    @Config("YuQ.Mirai.bot.master")
    public String master;
    @Inject
    private GroupService groupService;

    private Long updateTime = null;

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

    @Cron("1h")
    public void checkUpdate() throws IOException, ParseException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://hub.docker.com/v2/repositories/kukume/kukubot/");
        String timeStr = jsonObject.getString("last_updated");
        //2020-11-13T05:45:59.586192Z
        timeStr = timeStr.replace("T", " ");
        timeStr = timeStr.substring(0, timeStr.lastIndexOf("."));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date updateDate = sdf.parse(timeStr);
        long time = updateDate.getTime();
        if (updateTime != null){
            if (time > updateTime){
                List<GroupEntity> list = groupService.findAll();
                long group = list.get((int) (Math.random() * list.size())).getGroup();
                Map<Long, Member> members = FunKt.getYuq().getGroups().get(group).getMembers();
                Message message = Message.Companion.toMessage(
                        "程序有更新啦，快去更新吧。\n如果您使用的是docker版，请参考 https://www.kuku.me/archives/8/ 的更新教程\n" +
                                "如果不是，那么请手动替换最新jar包并重启：https://github.com/kukume/kuku-bot/actions，\n" +
                                "选择最新的workflows然后下载Artifacts"
                );
                long qq = Long.parseLong(master);
                if (members.containsKey(qq)){
                    members.get(qq).sendMessage(message);
                }else{
                    FunKt.getYuq().getFriends().get(qq).sendMessage(message);
                }
            }
        }
        updateTime = time;
    }
}
