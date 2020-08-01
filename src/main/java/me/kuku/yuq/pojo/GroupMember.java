package me.kuku.yuq.pojo;

public class GroupMember {
    private Long qq;
    private Integer level;
    private Integer integral;
    private Long joinTime;
    private Long lastTime;
    private Integer age;
    private String groupCard;

    public GroupMember(Long qq, Integer level, Integer integral, Long joinTime, Long lastTime, Integer age, String groupCard) {
        this.qq = qq;
        this.level = level;
        this.integral = integral;
        this.joinTime = joinTime;
        this.lastTime = lastTime;
        this.age = age;
        this.groupCard = groupCard;
    }

    public String getGroupCard() {
        return groupCard;
    }

    public void setGroupCard(String groupCard) {
        this.groupCard = groupCard;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public GroupMember() {
    }

    public Long getQq() {
        return qq;
    }

    public void setQq(Long qq) {
        this.qq = qq;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getIntegral() {
        return integral;
    }

    public void setIntegral(Integer integral) {
        this.integral = integral;
    }

    public Long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Long joinTime) {
        this.joinTime = joinTime;
    }

    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }
}
