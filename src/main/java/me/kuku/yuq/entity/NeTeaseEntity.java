package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "neTease")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NeTeaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private Long qq;
    private String MUSIC_U;
    private String __csrf;

    public NeTeaseEntity(String MUSIC_U, String __csrf){
        this.MUSIC_U = MUSIC_U;
        this.__csrf = __csrf;
    }

    public String getCookie(){
        return String.format("os=pc; osver=Microsoft-Windows-10-Professional-build-10586-64bit; appver=2.0.3.131777; channel=netease; __remember_me=true; MUSIC_U=%s; __csrf=%s; ", MUSIC_U, __csrf);
    }

    public NeTeaseEntity(Long qq){
        this.qq = qq;
    }
}
