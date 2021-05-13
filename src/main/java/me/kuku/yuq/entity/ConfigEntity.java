package me.kuku.yuq.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "config")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String type;
    @Lob
    @Column(columnDefinition = "text")
    private String content;

    public ConfigEntity(String type){
        this.type = type;
    }

    public JSONObject getContentJsonObject(){
        if (content == null) return new JSONObject();
        else return JSON.parseObject(content);
    }

    public JSONArray getContentJsonArray(){
        if (content == null) return new JSONArray();
        else return JSON.parseArray(content);
    }

    public <T> List<T> getContentList(Class<T> clazz){
        if (content == null) return new ArrayList<>();
        else return JSON.parseArray(content, clazz);
    }

    public void setContentJsonObject(JSONObject jsonObject){
        content = jsonObject.toString();
    }
}
