import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.7.22"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "me.kuku"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    maven("https://nexus.kuku.me/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("me.kuku:utils:2.2.1.2")
    implementation("net.mamoe:mirai-core:2.13.2")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xcontext-receivers")
}
