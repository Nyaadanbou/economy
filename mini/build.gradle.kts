plugins {
    id("economy-conventions.commons")
    id("nyaadanbou-conventions.repositories")
}

version = "1.0.0"

dependencies {
    // internal
    compileOnly(project(":api"))

    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(local.helper)

    // standalone plugins
    compileOnly(libs.miniplaceholders)
}
