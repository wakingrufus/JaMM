subprojects {
    repositories {
        mavenCentral()
    }
    version = "0.0.1-SNAPSHOT"
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "7.3"
    distributionType = Wrapper.DistributionType.ALL
}
