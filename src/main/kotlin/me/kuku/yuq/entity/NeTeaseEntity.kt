package me.kuku.yuq.entity

import javax.persistence.*

@Entity
@Table(name = "neTease")
data class NeTeaseEntity (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true)
        var qq: Long = 0L,
        var MUSIC_U: String = "",
        var __csrf: String = ""
){
    fun getCookie() = "os=pc; osver=Microsoft-Windows-10-Professional-build-10586-64bit; appver=2.0.3.131777; channel=netease; __remember_me=true; MUSIC_U=$MUSIC_U; __csrf=$__csrf; "
}