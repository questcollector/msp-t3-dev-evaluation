import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    kotlin("plugin.jpa") version "1.7.22"
    id("com.google.cloud.tools.jib") version "3.3.1"
    id("org.springdoc.openapi-gradle-plugin") version "1.6.0"
}

group = "com.samsung.sds.t3.dev"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

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
    testImplementation("io.mockk:mockk:1.13.4")

    // Spring Cloud Stream
    implementation("org.springframework.cloud:spring-cloud-stream-binder-rabbit:4.0.1")
    implementation("org.springframework.cloud:spring-cloud-function-kotlin:4.0.1")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder:4.0.1")

    // slack api client
    implementation("com.slack.api:slack-api-client:1.27.3")

    // mongodb
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.6.0")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.2")

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

jib {
    from {
        image = "amazoncorretto:17"
        platforms {
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "evaluation:test"
    }
}