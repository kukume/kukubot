package me.kuku.simbot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeiboPojo {
    private Long id;
    private String name;
    private String userId;
    private String created;
    private String text;
    private String bid;
    private List<String> imageUrl;
    private Boolean isForward;
    private String forwardId;
    private String forwardTime;
    private String forwardName;
    private String forwardText;
    private String forwardBid;

    public WeiboPojo(String name, String userId){
        this.name = name;
        this.userId = userId;
    }
}
