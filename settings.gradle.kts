rootProject.name = "GemsEconomy"

include(":velocity")
include(":bukkit")
include(":papi")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}