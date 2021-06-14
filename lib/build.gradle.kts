plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.4.31"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

dependencies {
    api("net.dv8tion", "JDA", "4.3.0_277") {
        exclude(module = "opus-java")
    }


    // Expose the JDK8 stdlib to end-users
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
