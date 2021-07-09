import java.io.File

plugins {
    kotlin("jvm") version "1.5.10"

    `java-library`
}

group = "net.greemdev"
version = "1.2"

repositories {
    maven("https://mvn.greemdev.net/repository/maven-central")
    maven("https://mvn.greemdev.net/repository/dv8tion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar {
        archiveFileName.set("${project.group}.KCommands-${archiveVersion.get()}.jar")
    }
}

dependencies {
    api("net.dv8tion", "JDA", "4.3.0_295") {
        exclude(module = "opus-java")
    }
}
