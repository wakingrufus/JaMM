pluginManagement {
    plugins {
        kotlin("multiplatform") version "1.4.30"
        kotlin("jvm") version "1.4.30"
        id("org.javamodularity.moduleplugin") version "1.8.10"
    }
}

rootProject.name = "jamm"
include(":common", ":desktop")
