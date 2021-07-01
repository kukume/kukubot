package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.BiliBiliEntity;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.pojo.BiliBiliPojo;
import me.kuku.yuq.service.BiliBiliService;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JobCenter
@SuppressWarnings("unused")
public class BiliBiliJob {
    @Inject
    private GroupService groupService;
    @Inject
    private BiliBiliLogic biliBiliLogic;
    @Inject
    private BiliBiliService biliBiliService;

    private final Map<Long, Map<Long, Long>> groupMap = new HashMap<>();
    private final Map<Long, Long> userMap = new HashMap<>();
    private final Map<Long, Map<Long, Boolean>> liveMap = new HashMap<>();

    @Cron("2m")
    public void biliBiliGroupMonitor() {
        List<GroupEntity> groupList = groupService.findAll();
        for (GroupEntity groupEntity: groupList){
            JSONArray biliBiliJsonArray = groupEntity.getBiliBiliJsonArray();
            Long group = groupEntity.getGroup();
            if (biliBiliJsonArray.size() == 0) continue;
            if (!groupMap.containsKey(group)){
                groupMap.put(group, new HashMap<>());
            }
            Map<Long, Long> biMap = groupMap.get(group);
            for (Object obj: biliBiliJsonArray){
                JSONObject jsonObject = (JSONObject) obj;
                Long userId = jsonObject.getLong("id");
                Result<List<BiliBiliPojo>> result;
                try {
                    result = biliBiliLogic.getDynamicById(userId.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                List<BiliBiliPojo> list = result.getData();
                if (list == null) continue;
                if (biMap.containsKey(userId)){
                    List<BiliBiliPojo> newList = new ArrayList<>();
                    for (BiliBiliPojo biliBiliPojo: list){
                        if (Long.parseLong(biliBiliPojo.getId()) <= biMap.get(userId)) break;
                        newList.add(biliBiliPojo);
                    }
                    newList.forEach(biliBiliPojo -> {
                        try {
                            FunKt.getYuq().getGroups().get(group).sendMessage(
                                    FunKt.getMif().text("哔哩哔哩有新动态了\n")
                                            .plus(biliBiliLogic.convertStr(biliBiliPojo))
                            );
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                }
                long newId = Long.parseLong(list.get(0).getId());
                if (!biMap.containsKey(userId) || newId > biMap.get(userId)){
                    biMap.put(userId, newId);
                }
            }
        }
    }

    @Cron("2m")
    public void biliBiliQQMonitor() throws IOException {
        List<BiliBiliEntity> biliBiliList = biliBiliService.findByMonitor(true);
        for (BiliBiliEntity biliBiliEntity: biliBiliList){
            Long qq = biliBiliEntity.getQq();
            Result<List<BiliBiliPojo>> result;
            try {
                result = biliBiliLogic.getFriendDynamic(biliBiliEntity);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            List<BiliBiliPojo> list = result.getData();
            if (list == null) continue;
            List<BiliBiliPojo> newList = new ArrayList<>();
            if (userMap.containsKey(qq)){
                Long oldId = userMap.get(qq);
                for (BiliBiliPojo biliBiliPojo: list){
                    if (Long.parseLong(biliBiliPojo.getId()) <= oldId) break;
                    newList.add(biliBiliPojo);
                }
                for (BiliBiliPojo biliBiliPojo: newList){
                    String userId = biliBiliPojo.getUserId();
                    List<JSONObject> likeList = BotUtils.match(biliBiliEntity.getLikeJsonArray(), userId);
                    if (likeList.size() != 0) biliBiliLogic.like(biliBiliEntity, biliBiliPojo.getId(), true);
                    List<JSONObject> commentList = BotUtils.match(biliBiliEntity.getCommentJsonArray(), userId);
                    for (JSONObject jsonObject: commentList) biliBiliLogic.comment(biliBiliEntity, biliBiliPojo.getRid(), biliBiliPojo.getType().toString(), jsonObject.getString("content"));
                    List<JSONObject> forwardList = BotUtils.match(biliBiliEntity.getForwardJsonArray(), userId);
                    for (JSONObject jsonObject: forwardList) biliBiliLogic.forward(biliBiliEntity, biliBiliPojo.getId(), jsonObject.getString("content"));
                    String bvId = biliBiliPojo.getBvId();
                    if (bvId != null){
                        List<JSONObject> tossCoinList = BotUtils.match(biliBiliEntity.getTossCoinJsonArray(), userId);
                        if (tossCoinList.size() != 0) biliBiliLogic.tossCoin(biliBiliEntity, biliBiliPojo.getRid(), 2);
                        List<JSONObject> favoritesList = BotUtils.match(biliBiliEntity.getFavoritesJsonArray(), userId);
                        for (JSONObject jsonObject: favoritesList) biliBiliLogic.favorites(biliBiliEntity, biliBiliPojo.getRid(), jsonObject.getString("content"));
                    }
                    try {
                        FunKt.getYuq().getGroups().get(biliBiliEntity.getGroup()).getMembers().get(qq)
                                .sendMessage(FunKt.getMif().text("哔哩哔哩有新动态了！！\n").plus(biliBiliLogic.convertStr(biliBiliPojo)));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            userMap.put(qq, Long.valueOf(list.get(0).getId()));
        }
    }

    @Cron("1m")
    public void liveMonitor(){
        List<BiliBiliEntity> list = biliBiliService.findAll();
        list.forEach( biliBiliEntity -> {
            Long qq = biliBiliEntity.getQq();
            JSONArray liveJsonArray = biliBiliEntity.getLiveJsonArray();
            if (!liveMap.containsKey(qq)) liveMap.put(qq, new HashMap<>());
            Map<Long, Boolean> map = liveMap.get(qq);
            liveJsonArray.forEach( obj -> {
                JSONObject jsonObject = (JSONObject) obj;
                Long id = jsonObject.getLong("id");
                try {
                    JSONObject liveJsonObject = biliBiliLogic.live(id.toString());
                    Boolean b = liveJsonObject.getBoolean("status");
                    if (map.containsKey(id)){
                        if (map.get(id) != b){
                            map.put(id, b);
                            String msg;
                            if (b) msg = "直播啦！！";
                            else msg = "下播了！！";
                            FunKt.getYuq().getGroups().get(biliBiliEntity.getGroup())
                                    .get(qq).sendMessage(
                                            BotUtils.toMessage("哔哩哔哩开播提醒：\n" +
                                                    jsonObject.getString("name") + msg + "\n" +
                                                    "标题：" + liveJsonObject.getString("title") + "\n" +
                                                    "链接：" + liveJsonObject.getString("url")
                            ));
                        }
                    }else map.put(id, b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Cron("At::d::08:00")
    public void biliBilliTask() throws IOException {
        List<BiliBiliEntity> list = biliBiliService.findByTask(true);
        for (BiliBiliEntity biliBiliEntity: list){
            List<Map<String, String>> ranking = biliBiliLogic.getRanking();
            Map<String, String> firstRank = ranking.get(0);
            biliBiliLogic.report(biliBiliEntity, firstRank.get("aid"), firstRank.get("cid"), 300);
            biliBiliLogic.share(biliBiliEntity, firstRank.get("aid"));
            biliBiliLogic.liveSign(biliBiliEntity);
            int[] arr = {2, 2, 1};
            for (int i = 0; i < 3; i++){
                Map<String, String> randomMap = ranking.get((int) (Math.random() * ranking.size()));
                biliBiliLogic.tossCoin(biliBiliEntity, randomMap.get("aid"), arr[i]);
            }
        }
    }
}