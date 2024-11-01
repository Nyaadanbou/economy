plugins {
    id("economy-conventions.commons")
    id("nyaadanbou-conventions.repositories")
    `maven-publish`
}

group = "cc.mewcraft.economy"
version = "2.0.1"
description = "A modern multi-currency economy API"

dependencies {
    // server
    compileOnly(libs.server.paper)
}

publishing {
    repositories {
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
                password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "economy-api"
            from(components["java"])
        }
    }
}