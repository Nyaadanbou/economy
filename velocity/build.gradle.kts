group = "me.xanium.gemseconomy"
version = "1.0.0"
description = "A modern multi-currency economy plugin on Velocity"

dependencies {
    // server
    compileOnly(libs.proxy.velocity)
    annotationProcessor(libs.proxy.velocity)

    // internal
    implementation(libs.hikari)
}
