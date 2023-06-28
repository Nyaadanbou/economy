plugins {
    id("cc.mewcraft.deploy-conventions")
    id("cc.mewcraft.paper-plugins")
}

project.ext.set("name", "GemsEconomy")

// name, version and description inherited from "project-conventions"

dependencies {
    implementation(project(":economy:api"))
    implementation(project(":economy:papi"))
    implementation(project(":economy:mini"))

    // the server api
    compileOnly(libs.server.paper)

    // my own libs
    compileOnly(project(":mewcore"))

    // libs that present as other plugins
    compileOnly(libs.helper)
    compileOnly(libs.helper.sql)
    compileOnly(libs.helper.redis)
    compileOnly(libs.connector.core)
    compileOnly(libs.connector.bukkit)
    compileOnly(libs.vault) {
        exclude("org.bukkit")
    }
}

// TODO remove/replace it with paper plugin specifications
/*bukkit {
    main = "me.xanium.gemseconomy.GemsEconomy"
    name = rootProject.name
    version = "${project.version}"
    apiVersion = "1.17"
    authors = listOf("Nailm", "other contributors")
    depend = listOf("helper", "MewCore")
    softDepend = listOf("Vault", "ConnectorPlugin")
    load = STARTUP
}*/

// commented - now managed by "cc.mewcraft.publishing-conventions"
/*publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "GemsEconomy"
            from(components["java"])
        }
    }
}*/
