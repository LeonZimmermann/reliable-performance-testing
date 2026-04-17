plugins {
    kotlin("jvm") version "2.0.21"
    id("io.gatling.gradle") version "3.11.5.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val generateGatlingPages = tasks.register<GenerateGatlingPagesTask>("generateGatlingPages") {
    specFile.set(file("../oas/openapi.yaml"))
    outputDir.set(layout.buildDirectory.dir("generated/gatling/kotlin"))
    packageName.set("org.example.gatling.pages")
}

sourceSets {
    named("gatling") {
        kotlin {
            srcDir(generateGatlingPages.flatMap { it.outputDir })
        }
    }
}

tasks.named("compileGatlingKotlin") {
    dependsOn(generateGatlingPages)
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Gatling
    gatling("io.gatling.highcharts:gatling-charts-highcharts:3.11.5")
    gatling("io.gatling:gatling-app:3.11.5")

    // Generated client dependencies
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.gsonfire:gson-fire:1.9.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
}

val baseUrl: String = project.findProperty("baseUrl")?.toString()
    ?: System.getenv("GATLING_BASE_URL")
    ?: "http://localhost:8080"

gatling {
    jvmArgs = listOf("-DbaseUrl=$baseUrl")
    // logLevel = "WARN"
}
