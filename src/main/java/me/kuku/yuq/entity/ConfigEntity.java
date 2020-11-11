package me.kuku.yuq.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
}
