rootProject.name = "GemsEconomy"

include(":plugin")
include(":papi")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}