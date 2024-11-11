plugins {
    id("nyaadanbou-conventions.repositories")
    id("economy-conventions.commons")
    `maven-publish`
}

group = "cc.mewcraft.economy"
version = "2.0.1"
description = "The API of Economy"

dependencies {
    compileOnly(local.paper)
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