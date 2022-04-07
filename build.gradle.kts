val utilsVersion = "0.3.17"
val yuCoreVersion = "0.2.0.0-DEV20"
val artQqVersion = "0.1.0.0-DEV15"
val webVersion = "0.0.2.0-DEV23"
val springDataJpaVersion = "2.6.3"
val springVersion = "5.3.17"
val hibernateVersion = "5.6.7.Final"
val hikariCPVersion = "4.0.3"
val h2Version = "1.4.200"
val hibernateTypesVersion = "2.14.1"
val jsoupVersion = "1.14.3"
val jsr305Version = "3.0.2"
val queryDslVersion = "5.0.0"
val telegramBotsVersion = "5.7.1"

plugins {
    val kotlinVersion = "1.6.20"
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "me.kuku"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://nexus.kuku.me/repository/maven-public/")
    mavenCentral()
}

tasks {
    val excludePath = arrayOf("conf/YuQ.properties")
    jar {
        exclude(*excludePath)
    }
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "me.kuku.yuq.StartKt"))
        }
        exclude(*excludePath)
    }
    build {
        finalizedBy(shadowJar)
    }
}


dependencies {
    // yuq
    implementation("com.IceCreamQAQ.YuQ:YuQ-ArtQQ:$artQqVersion")
    implementation("com.IceCreamQAQ.Yu:WebCore:$webVersion")
    implementation("com.IceCreamQAQ.Yu.WebCore.Server:SmartHTTP:$webVersion")
//    implementation("com.IceCreamQAQ:Yu-Core:$yuCoreVersion")

    // spring-data
    implementation("org.springframework.data:spring-data-jpa:$springDataJpaVersion")
    implementation("org.springframework:spring-aspects:$springVersion")
    implementation("org.hibernate:hibernate-core:$hibernateVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    implementation("com.vladmihalcea:hibernate-types-52:$hibernateTypesVersion")
    implementation("com.h2database:h2:$h2Version")
    implementation("com.querydsl:querydsl-core:$queryDslVersion")
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion:jpa")


    // telegram
    implementation("org.telegram:telegrambots:$telegramBotsVersion")
    implementation("org.telegram:telegrambots-abilities:$telegramBotsVersion")


    // other
    implementation("me.kuku:utils:$utilsVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("com.google.code.findbugs:jsr305:$jsr305Version")
}