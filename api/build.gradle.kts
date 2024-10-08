plugins {
    id("economy-conventions.commons")
    id("nyaadanbou-conventions.repositories")
    `maven-publish`
}

group = "me.xanium.gemseconomy"
version = "2.0.1"
description = "A modern multi-currency economy API"

dependencies {
    // server
    compileOnly(libs.server.paper)
}