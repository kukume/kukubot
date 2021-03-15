package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "hostloc")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostLocEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private Long qq;
    @Column(name = "group_")
    private Long group;
    private String username;
    private String password;
    @Lob
    @Column(columnDefinition="text")
    private String cookie;

    public HostLocEntity(Long qq){
        this.qq = qq;
    }
}
