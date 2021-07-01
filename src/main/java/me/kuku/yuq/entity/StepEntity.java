package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "motion")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StepEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private Long qq;
    @Column(name = "group_")
    private Long group;
    private String leXinPhone;
    private String leXinPassword;
    @Lob
    @Column(columnDefinition="text")
    private String leXinCookie;
    private String leXinUserId;
    @Lob
    @Column(columnDefinition="text")
    private String leXinAccessToken;
    private Boolean leXinStatus;
    private Integer step;
    private String miPhone;
    private String miPassword;
    @Lob
    @Column(columnDefinition="text")
    private String miLoginToken;
    private Boolean miStatus;

    public StepEntity(String leXinPhone, String leXinPassword, String leXinCookie, String leXinUserId, String leXinAccessToken){
        this.leXinPhone = leXinPhone;
        this.leXinPassword = leXinPassword;
        this.leXinCookie = leXinCookie;
        this.leXinUserId = leXinUserId;
        this.leXinAccessToken = leXinAccessToken;
    }

    public StepEntity(Long qq, Long group){
        this.group = group;
        this.leXinStatus = false;
        this.miStatus = false;
        this.qq = qq;
        this.step = 0;
    }

    public StepEntity(String miPhone, String miPassword, String miLoginToken){
        this.miPhone = miPhone;
        this.miPassword = miPassword;
        this.miLoginToken = miLoginToken;
    }
}
