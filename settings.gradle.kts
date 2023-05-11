pluginManagement {
    plugins {
    //    kotlin("multiplatform") version "1.5.30"
        kotlin("jvm") version "1.8.21"
        id("org.javamodularity.moduleplugin") version "1.8.12"
        id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("junit5", "5.8.1")
            library("junit-jupiter-api","org.junit.jupiter","junit-jupiter-api").versionRef("junit5")
            library("junit-jupiter-engine","org.junit.jupiter","junit-jupiter-engine").versionRef("junit5")
        }
    }
}
rootProject.name = "jamm"
include(":common", ":desktop")
