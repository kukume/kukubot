package me.kuku.yuq.pojo;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class ArkNightsUserPojo {
    //{ "name": "桃金娘", "type": "先锋", "level": 4, "sex": "女", "tags": [ "近战位", "治疗", "费用回复" ], "hidden": false, "name-en": "MyrtleF" }
    private String name;
    private String type;
    private int level;
    private String sex;
    private boolean hidden;
    @JSONField(name = "name-en")
    private String name_en;
    private List<String> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
