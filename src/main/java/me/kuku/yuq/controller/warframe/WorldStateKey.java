package me.kuku.yuq.controller.warframe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.smartboot.http.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FileName: WorldStateKey
 * Author:   wsure
 * Date:     2020/11/24 8:06 下午
 * Description:warframe-world-state 关键词
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public enum WorldStateKey {
    news("新闻"),
    events("事件"),
    alerts("警报"),
    sortie("突击"),
    Ostrons("地球赏金"),
    Solaris("金星赏金"),
    EntratiSyndicate("火卫二赏金"),
    fissures("裂缝"),
    flashSales("促销商品"),
    invasions("入侵"),
    voidTrader("奸商"),
    dailyDeals("特价"),
    persistentEnemies("小小黑"),
    earthCycle("地球"),
    cetusCycle("地球平原"),
    cambionCycle("火卫二平原"),
    vallisCycle("金星平原"),
    nightwave("电波"),
    arbitration("仲裁"),
    ;

    private final String keyWord;

    public static String getActionKey(){
        return getKeyWords()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public static String[] getSynonym(){
        return getKeyWords()
                .stream()
                .skip(1)
                .toArray(String[]::new);
    }

    public static List<String> getKeyWords(){
        return Arrays.stream(values())
                .map(WorldStateKey::getKeyWord)
                .collect(Collectors.toList());
    }

    public static String getNameByKeyWord(String keyWord){
        return Objects.requireNonNull(getByKeyWord(keyWord)).name();
    }

    public static WorldStateKey getByKeyWord(String keyWord){
        if(StringUtils.isBlank(keyWord)) return null;
        return Arrays.stream(values())
                .filter(e -> e.getKeyWord().equals(keyWord))
                .findFirst()
                .orElse(null);
    }

}
