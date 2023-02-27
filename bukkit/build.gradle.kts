import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP

plugins {
    id("cc.mewcraft.common")

    val indraVersion = "3.0.1"
    id("net.kyori.indra") version indraVersion
    id("net.kyori.indra.git") version indraVersion

    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
}

version = "${project.version}".decorateVersion()

dependencies {
    // The server API
    compileOnly("io.papermc.paper", "paper-api", "1.19.3-R0.1-SNAPSHOT")

    // 3rd party plugins
    compileOnlyApi("me.lucko", "helper", "5.6.13")
    compileOnly("de.themoep.connectorplugin", "core", "1.5-SNAPSHOT")
    compileOnly("de.themoep.connectorplugin", "bukkit", "1.5-SNAPSHOT")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7") {
        exclude("org.bukkit")
    }

    // External libraries (will be downloaded upon server startup)
    compileOnly("com.zaxxer", "HikariCP", "5.0.1")
    compileOnly("de.themoep.utils", "lang-bukkit", "1.3-SNAPSHOT")
    compileOnly("cloud.commandframework", "cloud-paper", "1.8.1")
    compileOnly("cloud.commandframework", "cloud-minecraft-extras", "1.8.1")
}

// TODO remove/replace it with paper plugin specifications
bukkit {
    main = "me.xanium.gemseconomy.GemsEconomy"
    name = rootProject.name
    version = "${project.version}"
    apiVersion = "1.17"
    authors = listOf("Nailm", "other contributors")
    depend = listOf("helper", "MewCore")
    softDepend = listOf("Vault", "ConnectorPlugin")
    load = STARTUP
    loadBefore = listOf("ItemFrameShops")
    libraries = listOf("com.zaxxer:HikariCP:5.0.1")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.version}.jar")
        archiveClassifier.set("shaded")
    }
    processResources {
        filesMatching("**/paper-plugin.yml") {
            val map = mapOf(
                "version" to "${project.version}",
                "description" to project.description
            )
            expand(map)
        }
    }
    task("deployToServer") {
        dependsOn(build)
        doLast {
            exec {
                commandLine("rsync", "${shadowJar.get().archiveFile.get()}", "dev:data/dev/jar")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "GemsEconomy"
            from(components["java"])
        }
    }
}

indra {
    javaVersions().target(17)
}

java {
    withSourcesJar()
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this
