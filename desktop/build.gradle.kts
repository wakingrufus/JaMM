plugins {
    application
    kotlin("jvm")
    id("org.openjfx.javafxplugin") version "0.0.10"
}

dependencies {
    implementation(project(":common", "jvmRuntimeElements"))
    implementation(kotlin("reflect"))
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.4.3")
    implementation("com.github.trilarion:java-vorbis-support:1.2.1")

    runtimeOnly("org.openjfx:javafx-graphics:$javafx.version:win")
    runtimeOnly("org.openjfx:javafx-graphics:$javafx.version:linux")
    runtimeOnly("org.openjfx:javafx-graphics:$javafx.version:mac")

    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.testfx:testfx-core:4.0.16-alpha")
    testImplementation("org.testfx:testfx-junit:4.0.16-alpha")
    testImplementation("org.testfx:openjfx-monocle:jdk-12.0.1+2")

}

javafx {
    version = "16"
    modules("javafx.controls", "javafx.media")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}



application {
    mainClass.set("com.github.wakingrufus.jamm.desktop.Main")
}


//jar {
//    manifest {
//        attributes( "Main-Class", "com.github.wakingrufus.organize.Launcher")
//    }
//    from {
//        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
//    }
//}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
