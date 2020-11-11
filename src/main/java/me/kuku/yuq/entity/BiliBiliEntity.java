package me.kuku.yuq.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "bilibili")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BiliBiliEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private Long qq;
    @Column(name = "group_")
    private Long group;
    @Lob
    @Column(columnDefinition = "text")
    private String cookie;
    private Boolean monitor;
    @Column(name = "task_")
    private Boolean task;
    @Lob
    @Column(columnDefinition = "text")
    private String liveList;
    @Lob
    @Column(columnDefinition = "text")
    private String likeList;
    @Lob
    @Column(columnDefinition = "text")
    private String commentList;
    @Lob
    @Column(columnDefinition = "text")
    private String forwardList;
    @Lob
    @Column(columnDefinition = "text")
    private String tossCoinList;
    @Lob
    @Column(columnDefinition = "text")
    private String favoritesList;
    private String token;
    private String userId;

    public BiliBiliEntity(String cookie, String userId, String token){
        this.cookie = cookie;
        this.userId = userId;
        this.token = token;
    }

    public BiliBiliEntity(Long qq, Long group){
        this.qq = qq;
        this.group = group;
    }

    public void setLiveJsonArray(JSONArray jsonArray){
        this.liveList = jsonArray.toString();
    }

    public JSONArray getLiveJsonArray(){
        if (liveList == null) return JSON.parseArray("[]");
        return JSON.parseArray(liveList);
    }

    public void setLikeJsonArray(JSONArray jsonArray){
        this.likeList = jsonArray.toString();
    }

    public JSONArray getLikeJsonArray(){
        if (likeList == null) return JSON.parseArray("[]");
        return JSON.parseArray(likeList);
    }

    public void setCommentJsonArray(JSONArray jsonArray){
        this.commentList = jsonArray.toString();
    }

    public JSONArray getCommentJsonArray(){
        if (commentList == null) return JSON.parseArray("[]");
        return JSON.parseArray(commentList);
    }

    public void setForwardJsonArray(JSONArray jsonArray){
        this.forwardList = jsonArray.toString();
    }

    public JSONArray getForwardJsonArray(){
        if (forwardList == null) return JSON.parseArray("[]");
        return JSON.parseArray(forwardList);
    }

    public void setTossCoinJsonArray(JSONArray jsonArray){
        this.tossCoinList = jsonArray.toString();
    }

    public JSONArray getTossCoinJsonArray(){
        if (tossCoinList == null) return JSON.parseArray("[]");
        return JSON.parseArray(tossCoinList);
    }

    public void setFavoritesJsonArray(JSONArray jsonArray){
        this.favoritesList = jsonArray.toString();
    }

    public JSONArray getFavoritesJsonArray(){
        if (favoritesList == null) return JSON.parseArray("[]");
        return JSON.parseArray(favoritesList);
    }
}
