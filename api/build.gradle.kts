plugins {
    id("cc.mewcraft.publishing-conventions")
}

group = "me.xanium.gemseconomy"
version = "2.0.1"
description = "A modern multi-currency economy API"

dependencies {
    // server
    compileOnly(libs.server.paper)
}