plugins {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "me.kuku"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://nexus.kuku.me/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("me.kuku:utils:2.2.3.0")
    implementation("me.kuku:ktor-spring-boot-starter:2.2.3.0")
    implementation("net.mamoe:mirai-core:2.13.4")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
}

kotlin {
    jvmToolchain(17)
}
