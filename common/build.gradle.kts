plugins {
    kotlin("jvm")
    id("org.javamodularity.moduleplugin")
}

dependencies {
    api("net.jthink:jaudiotagger:3.0.1")
    implementation(kotlin("reflect"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}