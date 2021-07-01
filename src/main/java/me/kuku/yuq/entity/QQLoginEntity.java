package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.kuku.utils.QqUtils;

import javax.persistence.*;

@Entity
@Table(name = "qqLogin")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QQLoginEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private Long qq;
    @Column(name = "group_")
    private Long group;
    private String password;
    private String sKey;
    private String psKey;
    private String groupPsKey;
    private String superKey;
    private String superToken;
    private String pt4Token;
    private Boolean status;

    public String getCookie(){
        return String.format("pt2gguin=o0%s; uin=o0%s; skey=%s; ", qq, qq, sKey);
    }

    public String getCookie(String psKey){
        return String.format("%sp_skey=%s; p_uin=o0%s;", getCookie(), psKey, qq);
    }

    public String getCookieWithQQZone(){
        return String.format("%sp_skey=%s; p_uin=o0%s; ", getCookie(), psKey, qq);
    }

    public String getCookieWithGroup(){
        return String.format("%sp_skey=%s; p_uin=o0%s; ", getCookie(), groupPsKey, qq);
    }

    public String getCookieWithSuper(){
        return String.format("superuin=o0%s; superkey=%s; supertoken=%s; ", qq, superKey, superToken);
    }

    public String getGtk(){
        return String.valueOf(QqUtils.getGTK(sKey));
    }

    public String getGtk(String psKey){
        return String.valueOf(QqUtils.getGTK(psKey));
    }

    public String getGtk2(){
        return QqUtils.getGTK2(sKey);
    }

    public String getGtkP(){
        return String.valueOf(QqUtils.getGTK(psKey));
    }

    public String getToken(){
        return String.valueOf(QqUtils.getToken(superToken));
    }

}