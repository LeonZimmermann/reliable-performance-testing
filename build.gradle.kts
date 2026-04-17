plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

group = "dev.leon.zimmermann.rpt"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

// Toolchains/JVM language level will be configured in each submodule
