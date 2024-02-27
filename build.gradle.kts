import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0-Beta2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "2.0.0-Beta2"
    id("org.sonarqube") version "4.4.1.3373"
    id("maven-publish")
}

group = "one.devsky"
version = "latest"

val jdaVersion: String by project
val exposedVersion: String by project

repositories {
    mavenCentral()
    maven("https://repo.flawcra.cc/mirrors")
}

val shadowDependencies = listOf(
    "ch.qos.logback:logback-classic:1.4.14",

    "com.google.code.gson:gson:2.10.1",
    "com.github.TheFruxz:Ascend:2023.3",
    "io.github.cdimascio:dotenv-kotlin:6.4.1",
    "net.dv8tion:JDA:$jdaVersion",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3",
    "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2",
    "net.oneandone.reflections8:reflections8:0.11.7",

    "org.jetbrains.exposed:exposed-core:$exposedVersion",
    "org.jetbrains.exposed:exposed-dao:$exposedVersion",
    "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
    "org.jetbrains.exposed:exposed-java-time:$exposedVersion",
    "com.mysql:mysql-connector-j:8.2.0",
    "org.mariadb.jdbc:mariadb-java-client:3.3.2",
    "com.zaxxer:HikariCP:5.1.0",

    "io.sentry:sentry:7.4.0",
    "io.sentry:sentry-kotlin-extensions:7.4.0",
    "io.sentry:sentry-logback:7.4.0",

    "io.javalin:javalin:5.6.3",

    "org.jsoup:jsoup:1.17.2",
    "com.openhtmltopdf:openhtmltopdf-core:1.0.10",
    "com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10",

    "dev.inmo:krontab:2.2.5",

    "com.aallam.openai:openai-client:3.6.2",
    "io.ktor:ktor-client-cio:2.3.7",

    "dev.arbjerg:lavaplayer:2.0.4"
)

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    shadowDependencies.forEach { dependency ->
        implementation(dependency)
        shadow(dependency)
    }
}

tasks {

    test {
        useJUnitPlatform()
    }

    build {
        dependsOn("shadowJar")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        configurations = listOf(project.configurations.shadow.get())
        archiveFileName.set("Spielehalle.jar")

        manifest {
            attributes["Main-Class"] = "one.devsky.spielehalle.StartKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

sonar {
    properties {
        property("sonar.projectKey", "Spielehalle")
        property("sonar.projectName", "Spielehalle")
    }
}

kotlin {
    jvmToolchain(21)
}