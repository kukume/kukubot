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
    @Config("YuQ.Mirai.bot.versionNo")
    private String versionNo;

    private int lastVersion = -1;

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

    @Cron("1s")
    public void checkUpdate() throws IOException {
        if (lastVersion == -1) lastVersion = Integer.parseInt(versionNo);
        JSONObject jsonObject = OkHttpUtils.getJson("https://api.kuku.me/bot/version/" + lastVersion);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("data");
        if (jsonArray.size() != 0){
            lastVersion = jsonArray.getJSONObject(0).getInteger("version");
            StringBuilder log = new StringBuilder();
            for (int i = 0; i < jsonArray.size(); i++){
                JSONObject singleJsonObject = jsonArray.getJSONObject(i);
                String version = singleJsonObject.getString("version");
                String ll = singleJsonObject.getString("log");
                log.append("版本号：").append(version).append("，更新日志：\n")
                        .append(ll).append("\n");
            }
            String logStr = BotUtils.removeLastLine(log);
            List<GroupEntity> list = groupService.findAll();
            long group = list.get((int) (Math.random() * list.size())).getGroup();
            Map<Long, Member> members = FunKt.getYuq().getGroups().get(group).getMembers();
            Message message = BotUtils.toMessage(
                    "程序有更新啦。日志如下：\n" + logStr
            );
            Message updateMessage = BotUtils.toMessage("更新方法：如果您使用的是docker版，请参考 https://www.kuku.me/archives/8/ 的更新教程，" +
                    "如果不是，那么请手动替换最新jar包并重启：https://file.kuku.me" );
            long qq = Long.parseLong(master);
            if (members.containsKey(qq)){
                Member member = members.get(qq);
                member.sendMessage(message);
                member.sendMessage(updateMessage);
            }else{
                Friend friend = FunKt.getYuq().getFriends().get(qq);
                friend.sendMessage(message);
                friend.sendMessage(updateMessage);
            }
        }
    }
}
