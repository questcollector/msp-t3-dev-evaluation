import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.jpa") version "1.9.0"
}

group = "com.samsung.sds.t3.dev"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
var mockkVersion = "1.13.4"
var springCloudStreamVersion = "4.0.1"
var slackClientVersion = "1.27.3"
var testcontainerVersion = "1.19.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    // caching
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Spring Cloud Stream
    implementation("org.springframework.cloud:spring-cloud-stream-binder-rabbit:$springCloudStreamVersion")
    implementation("org.springframework.cloud:spring-cloud-function-kotlin:$springCloudStreamVersion")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder:$springCloudStreamVersion")

    // slack api client
    implementation("com.slack.api:slack-api-client:$slackClientVersion")

    // mongodb
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    testImplementation("org.testcontainers:testcontainers:$testcontainerVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainerVersion")
    testImplementation("org.testcontainers:mongodb:$testcontainerVersion")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}