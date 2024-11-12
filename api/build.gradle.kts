plugins {
    id("nyaadanbou-conventions.repositories")
    id("economy-conventions.commons")
    `maven-publish`
}

group = "cc.mewcraft.economy"
version = "2.2.0"
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
            from(components["java"])
        }
    }
}