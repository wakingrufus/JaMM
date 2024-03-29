import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("org.openjfx.javafxplugin") version "0.0.14"
    id("org.beryx.jlink") version "2.26.0"
    id("org.javamodularity.moduleplugin")
}
project.description = "JavaFx Music Manager"
dependencies {
    implementation(project(":common"))
    implementation(kotlin("reflect"))
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.6.4")
    implementation("com.github.trilarion:java-vorbis-support:1.2.1")
    implementation("com.github.kittinunf.fuel:fuel-json:2.3.1")
    implementation("org.json:json:20200518")
    implementation("org.apache.commons:commons-csv:1.4")
    implementation("org.controlsfx:controlsfx:11.1.2")

    if (org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS)) {
        implementation("org.openjfx:javafx-base:${javafx.version}:win")
        implementation("org.openjfx:javafx-graphics:${javafx.version}:win")
    } else if (org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_UNIX)) {
        implementation("org.openjfx:javafx-base:${javafx.version}:linux")
        implementation("org.openjfx:javafx-graphics:${javafx.version}:linux")
    } else if (org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_MAC)) {
        implementation("org.openjfx:javafx-base:${javafx.version}:mac")
        implementation("org.openjfx:javafx-graphics:${javafx.version}:mac")
    }
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}
javafx {
    version = "21-ea+5"
    modules("javafx.base", "javafx.controls", "javafx.media")
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "jamm"
        jvmArgs = listOf("--add-opens=javafx.controls/javafx.scene.control.skin=jamm.desktop")
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
        requires("org.slf4j")
        requires("jdk.unsupported")
        requires("jdk.crypto.ec")
        provides("javax.sound.sampled.spi.AudioFileReader")
            .with("com.github.trilarion.sound.vorbis.sampled.spi.VorbisAudioFileReader")
        provides("kotlinx.coroutines.internal.MainDispatcherFactory")
            .with("kotlinx.coroutines.javafx.JavaFxDispatcherFactory")
        provides("javax.sound.sampled.spi.FormatConversionProvider")
            .with("com.github.trilarion.sound.vorbis.sampled.spi.VorbisFormatConversionProvider")
    }
    jpackage {
        if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
            installerOptions = listOf("--description", project.description, "--linux-shortcut")
            installerType = "deb"
        } else if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerOptions = listOf("--win-per-user-install", "--win-dir-chooser", "--win-menu", "--win-shortcut")
            imageOptions.add("--win-console")
            installerType = "msi"
        } else if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
            installerOptions = listOf("--description", project.description)
            installerType = "pkg"
        }
    }
}

application {
    mainModule.set("jamm.desktop")
    mainClass.set("com.github.wakingrufus.jamm.desktop.Main")
    applicationDefaultJvmArgs = listOf("--add-opens=javafx.controls/javafx.scene.control.skin=jamm.desktop")
}

tasks.withType<KotlinCompile> {
    kotlinOptions{
        languageVersion = "1.7"
    }
}
