// name and description inherited from "project-conventions"
version = "1.0.0"

dependencies {
    compileOnly(project(":economy:api"))

    // the server api
    compileOnly(libs.server.paper)

    // libs that present as other plugins
    compileOnly(libs.helper)
    compileOnly(libs.minipapi)
}
