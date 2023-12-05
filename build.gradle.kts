import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.*

val kotlinRepositoryPath: String? by project

plugins {
    kotlin("jvm") version "2.0.255-SNAPSHOT"
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

kotlin {
    compilerOptions.languageVersion.set(KOTLIN_2_0)
}

application {
    mainClass.set("MainKt")
}
