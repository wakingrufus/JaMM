subprojects {
    repositories {
        mavenCentral()
    }
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "6.9"
    distributionType = Wrapper.DistributionType.ALL
}
