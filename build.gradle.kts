plugins {
    val kotlinVersion = "1.8.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "me.kuku"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://nexus.kuku.me/repository/maven-public/")
    maven("https://repo.mirai.mamoe.net/snapshots")
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("me.kuku:utils:2.3.0.0")
    implementation("me.kuku:ktor-spring-boot-starter:2.3.0.0")
    implementation("net.mamoe:mirai-core:2.15.0-dev-93")
    implementation("org.jsoup:jsoup:1.15.3")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
}

tasks.compileJava {
    options.encoding = "utf-8"
}

kotlin {
    jvmToolchain(17)
}
