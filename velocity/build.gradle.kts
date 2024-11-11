plugins {
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    id("economy-conventions.commons")
}

group = "cc.mewcraft.economy"
version = "1.0.0"
description = "The Velocity plugin of Economy"

dependencies {
    compileOnly(local.velocity)
    annotationProcessor(local.velocity)
    implementation(libs.hikari)
}

tasks {
    copyJar {
        environment = "velocity"
        jarFileName = "economy-${project.version}.jar"
    }
}
