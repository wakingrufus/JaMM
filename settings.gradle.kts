pluginManagement {
    plugins {
        kotlin("multiplatform") version "1.4.30"
        kotlin("jvm") version "1.4.30"
    }
}

rootProject.name = "jamm"
include(":common", ":desktop")
