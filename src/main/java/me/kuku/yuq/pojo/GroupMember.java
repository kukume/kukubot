package me.kuku.yuq.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember {
    private Long qq;
    private Integer level;
    private Integer integral;
    private Long joinTime;
    private Long lastTime;
    private Integer age;
    private String groupCard;
    private String nickName;
    private String country;
    private String province;
    private String city;
    private Integer userAge;

    public GroupMember(Long qq, Long joinTime, Long lastTime, Integer age, String groupCard){
        this.qq = qq;
        this.joinTime = joinTime;
        this.lastTime = lastTime;
        this.age = age;
        this.groupCard = groupCard;
    }

    public  GroupMember(Long qq, Integer level, Integer integral, Long joinTime, Long lastTime){
        this.qq = qq;
        this.level = level;
        this.integral = integral;
        this.joinTime = joinTime;
        this.lastTime = lastTime;
    }
}
