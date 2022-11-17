subprojects {
    repositories {
        mavenCentral()
    }
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "7.5.1"
    distributionType = Wrapper.DistributionType.ALL
}
