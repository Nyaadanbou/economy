plugins {
    id("cc.mewcraft.project-conventions")
}

// name and description inherited from "project-conventions"
version = "1.0.0"

dependencies {
    // the proxy api
    compileOnly(libs.proxy.velocity)
    annotationProcessor(libs.proxy.velocity)

    // libs to be shaded
    compileOnly(libs.hikari)

    // libs that present as other plugins
    compileOnly(libs.luckperms)
}
