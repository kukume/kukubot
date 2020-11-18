package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwitterPojo implements Comparable<TwitterPojo> {
    private Long userId;
    private String name;
    private String screenName;
    private Long tweetsId;
    private String creatAt;
    private String text;
    private String url;
    private List<String> picList;

    public TwitterPojo(Long userId, String name, String screenName){
        this.userId = userId;
        this.name = name;
        this.screenName = screenName;
    }

    @Override
    public int compareTo(@NotNull TwitterPojo o) {
        return Long.compare(o.getTweetsId(), tweetsId);
    }
}
