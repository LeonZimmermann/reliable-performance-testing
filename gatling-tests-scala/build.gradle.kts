plugins {
    id("scala")
    id("io.gatling.gradle") version "3.11.5.2"
    id("org.openapi.generator") version "7.12.0"
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

openApiGenerate {
    generatorName.set("scala-gatling")
    inputSpec.set(file("../oas/openapi.yaml").absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/gatling").map { it.asFile.absolutePath })
    apiPackage.set("org.example.gatling")
    modelPackage.set("org.example.gatling.model")
    configOptions.set(mapOf("sourceFolder" to "src/gatling/scala"))
}

sourceSets {
    named("gatling") {
        scala {
            srcDir(layout.buildDirectory.dir("generated/gatling/src/gatling/scala"))
        }
    }
}

tasks.named("compileGatlingScala") {
    dependsOn(tasks.named("openApiGenerate"))
}

dependencies {
    gatling("io.gatling.highcharts:gatling-charts-highcharts:3.11.5")
    gatling("io.gatling:gatling-app:3.11.5")
}
