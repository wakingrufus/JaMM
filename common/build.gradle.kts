plugins {
    kotlin("jvm")
    id("org.javamodularity.moduleplugin")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.30")
    api("net.jthink:jaudiotagger:3.0.1")
    implementation(kotlin("reflect"))
}

