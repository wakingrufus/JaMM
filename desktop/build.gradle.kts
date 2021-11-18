plugins {
    application
    kotlin("jvm")
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.24.1"
    id("org.javamodularity.moduleplugin")
}

dependencies {
    implementation(project(":common"))
    implementation(kotlin("reflect"))
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.4.3")
    implementation("com.github.trilarion:java-vorbis-support:1.2.1")
    implementation("javax.servlet:javax.servlet-api:4.0.1")

    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:win")
    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:linux")
    runtimeOnly("org.openjfx:javafx-graphics:${javafx.version}:mac")

    testImplementation("org.assertj:assertj-core:3.11.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
javafx {
    version = "16"
    modules("javafx.controls", "javafx.media")
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "jamm"
    }
    forceMerge("javafx")
    //addExtraDependencies("javafx")
    mergedModule {
        requires("java.management")
        requires("java.naming")
        requires("java.logging")
        requires("java.xml")
        requires("java.desktop")
        requires("java.instrument")
        requires("jdk.jfr")
        requires("jdk.unsupported")
        provides("javax.sound.sampled.spi.AudioFileReader")
            .with ("com.github.trilarion.sound.vorbis.sampled.spi.VorbisAudioFileReader")
        provides("kotlinx.coroutines.internal.MainDispatcherFactory")
            .with ("kotlinx.coroutines.javafx.JavaFxDispatcherFactory")
        provides("javax.sound.sampled.spi.FormatConversionProvider")
            .with ("com.github.trilarion.sound.vorbis.sampled.spi.VorbisFormatConversionProvider")
    }
    jpackage {
        installerOptions = listOf("--description", project.description)
        installerType = "deb"
        installerOptions = listOf("--linux-shortcut")
    }
}

application {
    mainModule.set("jamm.desktop")
    mainClass.set("com.github.wakingrufus.jamm.desktop.Main")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
