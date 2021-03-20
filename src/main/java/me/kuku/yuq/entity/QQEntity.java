package me.kuku.yuq.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "QQ")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QQEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long qq;
    @OneToOne
    @JoinColumn(name = "group_id")
    private GroupEntity groupEntity;
    private Integer violationCount;
    @Lob
    @Column(columnDefinition="text")
    private String twitterList;
    private boolean hostLocPush;

    public QQEntity(long qq, GroupEntity groupEntity){
        this.qq = qq;
        this.groupEntity = groupEntity;
        this.violationCount = 0;
        this.hostLocPush = false;
    }

    public JSONArray getTwitterJsonArray(){
        if (twitterList == null) return new JSONArray();
        else return JSON.parseArray(twitterList);
    }

    public void setTwitterJsonArray(JSONArray jsonArray){
        this.twitterList = jsonArray.toString();
    }
}