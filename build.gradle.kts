subprojects {
    repositories {
        mavenCentral()
    }
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.ALL
}
