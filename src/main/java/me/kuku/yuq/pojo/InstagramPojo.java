package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstagramPojo {
    private Long userId;
    private String name;
    private String fullName;
    private Long id;
    private List<String> picList;

    public InstagramPojo(Long userId, String name, String fullName){
        this.userId = userId;
        this.name = name;
        this.fullName = fullName;
    }
}
