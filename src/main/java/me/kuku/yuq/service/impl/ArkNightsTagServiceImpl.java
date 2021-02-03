package me.kuku.yuq.service.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ArkNightsUserEntity;
import me.kuku.yuq.service.ArkNightsTagService;
import me.kuku.yuq.utils.OkHttpUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArkNightsTagServiceImpl implements ArkNightsTagService {
    private static List<ArkNightsUserEntity> arkNightsUserEntities = null;

    public static void getInstance() {
        if (arkNightsUserEntities == null) {
            try {
                arkNightsUserEntities = JSONObject.parseArray(OkHttpUtils.getStr("https://www.bigfun.cn/static/aktools/1611029120/data/akhr.json"), ArkNightsUserEntity.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String arkTagInfo(List<String> tags) {
        getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("检测到可选tag\n");
        HashMap<List<String>, List<ArkNightsUserEntity>> map = new HashMap<>();
        LinkedList<String> mapTagList = new LinkedList<>();
        swap(tags, map, 3, 0, 0, mapTagList); // 从这个数组5个数中选择三个
        for (Map.Entry<List<String>, List<ArkNightsUserEntity>> listListEntry : map.entrySet()) {
            List<ArkNightsUserEntity> value = listListEntry.getValue();
            List<String> entryKey = listListEntry.getKey();
            if (value.isEmpty()) {
                continue;
            }
            if (value.stream().allMatch(ArkNightsUserEntity -> ArkNightsUserEntity.getLevel() > 3 || ArkNightsUserEntity.getLevel() == 1)) {
                stringBuilder.append(entryKey).append(value.stream().map(ArkNightsUserEntity::getName).collect(Collectors.toList())).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @param tags       公招标签
     * @param map        key 不同组合的标签 value 包含这种标签的干员
     * @param target     从tags要选择的个数
     * @param has        当前有多少元素
     * @param cur        当前索引
     * @param mapTagList 标签组合 相当于map的key
     */
    public static void swap(List<String> tags, HashMap<List<String>, List<ArkNightsUserEntity>> map, int target, int has, int cur, LinkedList<String> mapTagList) {
        ArrayList<String> arrayList = new ArrayList<>(mapTagList);
        if (!arrayList.isEmpty()) {
            map.put(arrayList, arkNightsUserEntities.stream().filter(ArkNightsUserEntity -> Stream.concat(Arrays.stream(ArkNightsUserEntity.getTags().toArray()), Stream.of(ArkNightsUserEntity.getType())).collect(Collectors.toList()).containsAll(arrayList)).collect(Collectors.toList()));
            if (has == target) {
                return;
            }
        }
        for (int i = cur; i < tags.size(); i++) {
            if (!mapTagList.contains(tags.get(i))) {
                mapTagList.add(tags.get(i));
                swap(tags, map, target, has + 1, i, mapTagList);
                mapTagList.removeLast();
            }
        }
    }
}
