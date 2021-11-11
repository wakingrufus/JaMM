plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm("jvm")
    linuxX64("linux")

    sourceSets {
        named("commonMain") {
            dependencies {
                implementation("net.jthink:jaudiotagger:3.0.1")
            }
        }
    }
}
