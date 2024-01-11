import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.jpa") version "1.9.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.6.0"
}

group = "com.samsung.sds.t3.dev"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
var mockkVersion = "1.13.4"
var testcontainerVersion = "1.19.3"
var springdocVersion = "2.0.2"

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

    // mongodb
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    testImplementation("org.testcontainers:testcontainers:$testcontainerVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainerVersion")
    testImplementation("org.testcontainers:mongodb:$testcontainerVersion")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:$springdocVersion")

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

openApi {
    apiDocsUrl.set("http://localhost:8085/v3/api-docs/messageData")
    outputDir.set(file("$projectDir/docs"))
    outputFileName.set("messageData.json")
    waitTimeInSeconds.set(10)
    groupedApiMappings.put(
        "http://localhost:8085/v3/api-docs/messageData",
        "messageData.json")
    customBootRun {
        args.add("--spring.profiles.active=dev")
    }
}