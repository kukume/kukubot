package me.kuku.yuq.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "QQJob")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QQJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long qq;
    private String type;
    @Lob
    @Column(columnDefinition="text")
    private String data;

    public JSONObject getDataJsonObject(){
        if (data == null) return new JSONObject();
        else return JSON.parseObject(data);
    }

    public void setDataJsonObject(JSONObject jsonObject){
        this.data = jsonObject.toString();
    }
}