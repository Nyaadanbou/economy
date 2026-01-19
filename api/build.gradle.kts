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
        maven("https://repo.mewcraft.cc/releases") {
            credentials {
                username = providers.gradleProperty("nyaadanbouReleasesUsername").orNull
                password = providers.gradleProperty("nyaadanbouReleasesPassword").orNull
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}