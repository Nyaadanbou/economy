pluginManagement {
    repositories {
        mavenLocal() // 为了导入 "nyaadanbou-repositories"
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("nyaadanbou-repository") version "0.0.1-snapshot"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("local") {
            from(files("gradle/local.versions.toml"))
        }
    }
    versionCatalogs {
        create("libs") {
            from("cc.mewcraft.gradle:catalog:1.0-SNAPSHOT")
        }
    }
}

rootProject.name = "economy"

include("api")
include("bukkit")
include("mini")
include("papi")
include("velocity")