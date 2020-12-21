package me.kuku.yuq.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "weibo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeiboEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private Long qq;
    @Column(name = "group_")
    private Long group;
    @Lob
    @Column(columnDefinition="text")
    private String pcCookie;
    @Lob
    @Column(columnDefinition="text")
    private String mobileCookie;
    private Boolean monitor;
    @Lob
    @Column(columnDefinition="text")
    private String likeList;
    @Lob
    @Column(columnDefinition="text")
    private String commentList;
    @Lob
    @Column(columnDefinition="text")
    private String forwardList;

    public WeiboEntity(String pcCookie, String mobileCookie){
        this.pcCookie = pcCookie;
        this.mobileCookie = mobileCookie;
    }

    public WeiboEntity(Long qq, Long group){
        this.qq = qq;
        this.group = group;
    }

    public JSONArray getLikeJsonArray(){
        if (likeList == null) return new JSONArray();
        else return JSON.parseArray(likeList);
    }

    public void setLikeJsonArray(JSONArray jsonArray){
        this.likeList = jsonArray.toString();
    }

    public JSONArray getCommentJsonArray(){
        if (commentList == null) return new JSONArray();
        else return JSON.parseArray(commentList);
    }

    public void setCommentJsonArray(JSONArray jsonArray){
        this.commentList = jsonArray.toString();
    }

    public JSONArray getForwardJsonArray(){
        if (forwardList == null) return new JSONArray();
        else return JSON.parseArray(forwardList);
    }

    public void setForwardJsonArray(JSONArray jsonArray){
        this.forwardList = jsonArray.toString();
    }
}
