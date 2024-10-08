val kotlinRepositoryPath: String? by project

plugins {
    kotlin("jvm") version "2.0.20"
    application
}

repositories {
    mavenCentral()
    kotlinRepositoryPath?.let {
        maven {
            url = uri("file://$it")
        }
    }
}

application {
    mainClass.set("MainKt")
}
