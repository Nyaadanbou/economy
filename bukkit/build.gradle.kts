plugins {
    id("cc.mewcraft.project-conventions")
    id("cc.mewcraft.publishing-conventions")
    alias(libs.plugins.indra)
}

// name, version and description inherited from "project-conventions"

dependencies {
    // the server api
    compileOnly(libs.server.paper)

    // my own libs
    compileOnly(libs.mewcore)

    // libs that present as other plugins
    compileOnlyApi(libs.helper)
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

tasks {
    jar {
        archiveBaseName.set(rootProject.name);
    }
    processResources {
        filesMatching("**/paper-plugin.yml") {
            expand(
                mapOf(
                    "version" to "${project.version}",
                    "description" to project.description
                )
            )
        }
    }
    task("deployToServer") {
        dependsOn(build)
        doLast {
            exec {
                commandLine("rsync", "${jar.get().archiveFile.get()}", "dev:data/dev/jar")
            }
        }
    }
}

// commented - now managed by "cc.mewcraft.publishing-conventions"
/*publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "GemsEconomy"
            from(components["java"])
        }
    }
}*/

indra {
    javaVersions().target(17)
}
