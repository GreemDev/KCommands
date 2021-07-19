plugins {
    kotlin("jvm") version "1.5.21"

    `java-library`
}

group = "net.greemdev"
version = "1.3"

repositories {
    maven("https://mvn.greemdev.net/repository/maven-central")
    maven("https://mvn.greemdev.net/repository/dv8tion")
    mavenCentral()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar {
        archiveFileName.set("lib.jar")
    }
}

dependencies {
    api("net.dv8tion", "JDA", "4.3.0_297") {
        exclude(module = "opus-java")
    }
}