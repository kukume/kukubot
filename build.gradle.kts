val utilsVersion = "0.3.4"
val artQqVersion = "0.1.0.0-DEV10"
val webVersion = "0.0.2.0-DEV16"
val springDataJpaVersion = "2.6.0"
val hibernateVersion = "5.6.3.Final"
val h2Version = "1.4.200"
val hibernateTypesVersion = "2.14.0"
val jsoupVersion = "1.14.3"
val jsr305Version = "3.0.2"

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "me.kuku"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://nexus.kuku.me/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("me.kuku:utils:$utilsVersion")
    implementation("com.IceCreamQAQ.YuQ:YuQ-ArtQQ:$artQqVersion")
    implementation("com.IceCreamQAQ.Yu:WebCore:$webVersion")
    implementation("org.springframework.data:spring-data-jpa:$springDataJpaVersion")
    implementation("org.hibernate:hibernate-hikaricp:$hibernateVersion")
    implementation("com.vladmihalcea:hibernate-types-52:$hibernateTypesVersion")
    implementation("com.h2database:h2:$h2Version")
    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("com.google.code.findbugs:jsr305:$jsr305Version")
}