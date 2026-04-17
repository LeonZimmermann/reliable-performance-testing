plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "reliable-performance-testing"
include(":oas", ":backend", ":gatling-tests")
