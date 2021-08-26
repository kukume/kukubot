package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BiliBiliPojo {
    private String userId;
    private String name;
    private String id;
    private String rid;
    private Integer type;
    private Long time;
    private String text;
    private String bvId;
    private List<String> picList = new ArrayList<>();
    private Boolean isForward;
    private String forwardUserId;
    private String forwardName;
    private String forwardId;
    private Long forwardTime;
    private String forwardText;
    private String forwardBvId;
    private List<String> forwardPicList = new ArrayList<>();
    public BiliBiliPojo(String userId, String name){
        this.userId = userId;
        this.name = name;
    }
}
