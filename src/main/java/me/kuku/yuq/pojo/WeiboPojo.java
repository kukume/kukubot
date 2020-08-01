package me.kuku.yuq.pojo;

import java.util.List;

public class WeiboPojo {
    private String id;
    private String name;
    private String created;
    private String text;
    private String bid;
    private List<String> imageUrl;

    @Override
    public String toString() {
        return "WeiboPojo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", created='" + created + '\'' +
                ", text='" + text + '\'' +
                ", bid='" + bid + '\'' +
                ", imageUrl=" + imageUrl +
                '}';
    }

    public WeiboPojo(String id, String name, String created, String text, String bid, List<String> imageUrl) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.text = text;
        this.bid = bid;
        this.imageUrl = imageUrl;
    }

    public WeiboPojo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public List<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(List<String> imageUrl) {
        this.imageUrl = imageUrl;
    }
}
