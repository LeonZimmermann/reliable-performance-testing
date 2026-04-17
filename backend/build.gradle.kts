plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    id("org.openapi.generator") version "7.4.0"
}

group = "dev.leon.zimmermann.rpt"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories { mavenCentral() }

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.5"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

sourceSets {
    main {
        kotlin.srcDir("$buildDir/generated/sources/openapi/src/main/kotlin")
    }
}

tasks.named<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerate") {
    generatorName.set("kotlin-spring")
    inputSpec.set(project.layout.projectDirectory.file("../oas/openapi.yaml").asFile.absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/sources/openapi").get().asFile.absolutePath)
    apiPackage.set("dev.leon.zimmermann.rpt.generated.api")
    modelPackage.set("dev.leon.zimmermann.rpt.generated.model")
    invokerPackage.set("dev.leon.zimmermann.rpt.generated.invoker")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "useSpringBoot3" to "true",
            "documentationProvider" to "none",
            "exceptionHandler" to "false",
            "hideGenerationTimestamp" to "true",
            "dateLibrary" to "java8",
            "useSwaggerAnnotations" to "false",
            "useTags" to "true",
            "serializationLibrary" to "jackson",
            "useResponseEntity" to "false",
            "reactive" to "false"
        )
    )
}

tasks.named("compileKotlin") { dependsOn("openApiGenerate") }
tasks.named("compileJava") { dependsOn("openApiGenerate") }

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    jvmArgs = listOf("-Dspring.output.ansi.enabled=ALWAYS")
}
