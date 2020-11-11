package me.kuku.yuq.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "message", indexes = {@Index(name = "idx_group", columnList = "group_"), @Index(name = "idx_messageId", columnList = "messageId")})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer messageId;
    @Column(name = "group_")
    private Long group;
    private Long qq;
    @Lob
    @Column(columnDefinition = "text")
    private String content;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    public JSONArray getContentJsonArray(){
        if (content == null) return JSON.parseArray("[]");
        else return JSON.parseArray(content);
    }

    public void setContentJsonArray(JSONArray jsonArray){
        this.content = jsonArray.toString();
    }
}