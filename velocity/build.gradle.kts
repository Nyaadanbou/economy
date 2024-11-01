plugins {
    id("economy-conventions.commons")
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    `maven-publish`
}

group = "cc.mewcraft.economy"
version = "1.0.0"
description = "A modern multi-currency economy plugin on Velocity"

dependencies {
    // server
    compileOnly(libs.proxy.velocity)
    annotationProcessor(libs.proxy.velocity)

    // internal
    implementation(libs.hikari)
}

tasks {
    copyJar {
        environment = "velocity"
        jarFileName = "economy-${project.version}.jar"
    }
}
