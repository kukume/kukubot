package me.kuku.yuq.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "message", indexes = {@Index(name = "idx_group", columnList = "group_"), @Index(name = "idx_messageId", columnList = "messageId")})
@Getter
@Setter
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

    public boolean equals(Object obj){
        if (obj instanceof MessageEntity){
            JSONArray contentJsonArray1 = getContentJsonArray();
            JSONArray contentJsonArray2 = ((MessageEntity) obj).getContentJsonArray();
            if (contentJsonArray1.size() != contentJsonArray2.size()) return false;
            for (int i = 0; i < contentJsonArray1.size(); i++){
                JSONObject jsonObject1 = contentJsonArray1.getJSONObject(i);
                JSONObject jsonObject2 = contentJsonArray2.getJSONObject(i);
                String type1 = jsonObject1.getString("type");
                String type2 = jsonObject2.getString("type");
                if (!type1.equals(type2)) return false;
                if (type1.equals("image")){
                    if (!jsonObject1.getString("id").equals(jsonObject2.getString("id"))) return false;
                }else {
                    if (!jsonObject1.getString("content").equals(jsonObject2.getString("content"))) return false;
                }
            }
        } else return false;
        return true;
    }
}