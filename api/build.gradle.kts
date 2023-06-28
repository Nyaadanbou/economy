plugins {
    id("cc.mewcraft.publishing-conventions")
}

dependencies {
    // the server api
    compileOnly(libs.server.paper)
}