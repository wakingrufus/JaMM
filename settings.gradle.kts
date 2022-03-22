enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories{
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap/")
    }
    plugins {
        kotlin("multiplatform") version "1.5.30"
        kotlin("jvm") version "1.5.30"
        kotlin("android") version "1.6.10"
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
include(":common", ":desktop", ":android")
