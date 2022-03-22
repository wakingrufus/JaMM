buildscript {
    repositories{
        google()
    }
    dependencies{
        classpath("com.android.tools.build:gradle:7.0.4")
    }
}
plugins {
    id("com.quittle.setup-android-sdk") version "2.1.0"
}

subprojects {
    repositories {
        mavenCentral()
    }
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "7.3"
    distributionType = Wrapper.DistributionType.ALL
}

//setupAndroidSdk {
//    packages ("ndk-bundle", "emulator", "system-images;android-28;default;x86", "system-images;android-30;google_apis;x86")
//}