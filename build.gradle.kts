plugins {
    kotlin("jvm") version "1.4.31"


    id("com.github.johnrengelman.shadow") version "6.1.0"

    `maven-publish`

    `java-library`
}

group = "com.github.GreemDev"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    shadowJar {
        fun dest(packageName: String): String {
            return "net.greemdev.kcommands.lib.$packageName"
        }
        relocate("net.dv8tion.jda", dest("jda"))
    }
}

dependencies {
    api("net.dv8tion", "JDA", "4.3.0_277") {
        exclude(module = "opus-java")
    }

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
