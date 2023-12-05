
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        val kotlinRepositoryPath: String? by settings
        kotlinRepositoryPath?.let {
            maven {
                url = uri("file://$it")
            }
        }
    }
}
