import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val queryDslVersion = "5.0.0"

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
}

group = "me.kuku"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    maven("https://nexus.kuku.me/repository/maven-public/")
    mavenCentral()
}

dependencies {
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("me.kuku:utils-fastjson:0.0.1")
    implementation("com.IceCreamQAQ.YuQ:YuQ-ArtQQ:0.1.0.0-DEV17")
    implementation("com.IceCreamQAQ:Yu-Core:0.2.0.0-DEV23")
    implementation("com.IceCreamQAQ:YuQ:0.1.0.0-DEV31")
    implementation("com.h2database:h2")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.vladmihalcea:hibernate-types-55:2.16.1")
    implementation("com.querydsl:querydsl-core:$queryDslVersion")
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion:jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
