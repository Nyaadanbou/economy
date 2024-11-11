plugins {
    id("nyaadanbou-conventions.repositories")
    id("economy-conventions.commons")
}

version = "1.0.0"

dependencies {
    compileOnly(project(":api"))
    compileOnly(local.paper)
    compileOnly(local.helper)
    compileOnly(local.miniplaceholders)
}
