enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    plugins {
        kotlin("multiplatform") version "1.5.30"
        kotlin("jvm") version "1.5.30"
        id("org.javamodularity.moduleplugin") version "1.8.10"
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("junit5", "5.8.1")
            alias("junit-jupiter-api").to("org.junit.jupiter","junit-jupiter-api").versionRef("junit5")
            alias("junit-jupiter-engine").to("org.junit.jupiter","junit-jupiter-engine").versionRef("junit5")
        }
    }
}
rootProject.name = "jamm"
include(":common", ":desktop")
