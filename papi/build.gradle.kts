// name and description inherited from "project-conventions"
version = "1.0.0"

dependencies {
    // internal
    compileOnly(project(":economy:api"))

    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(libs.helper)

    // standalone plugins
    compileOnly(libs.papi)
}
