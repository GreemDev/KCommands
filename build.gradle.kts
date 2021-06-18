plugins {
    kotlin("jvm") version "1.5.10"

    `java-library`
}

group = "net.greemdev"
version = "1.1"

repositories {
    maven("https://mvn.greemdev.net/repository/maven-central")
    maven("https://m2.dv8tion.net/releases")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar {
        archiveFileName.set("lib.jar")
    }
}

dependencies {
    api("net.dv8tion", "JDA", "4.3.0_277") {
        exclude(module = "opus-java")
    }

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
