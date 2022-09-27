import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("net.kyori.indra") version "2.1.1"
    id("net.kyori.indra.git") version "2.1.1"
}

group = "me.xanium.gemseconomy"
version = "1.2-SNAPSHOT".decorateVersion()
description = "A multi-currency economy plugin for spigot servers"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.purpurmc.org/snapshots") {
        content {
            includeGroup("org.purpurmc.purpur")
        }
    }
    maven("https://jitpack.io") {
        content {
            includeGroup("com.github.MilkBowl")
        }
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        content {
            includeGroup("org.purpurmc.purpur")
        }
    }
}

dependencies {
    // API
    compileOnly("org.purpurmc.purpur","purpur-api", "1.19.2-R0.1-SNAPSHOT")

    // Plugin libraries
    compileOnly("me.lucko", "helper" ,"5.6.10")
    compileOnly("com.github.MilkBowl","VaultAPI","1.7")
    compileOnly("dev.jorel","commandapi-core","8.5.1")
    compileOnly("org.jetbrains","annotations","23.0.0")

    // Libraries that needs to be shaded
    implementation("cloud.commandframework","cloud-paper","1.7.1")
    implementation("cloud.commandframework", "cloud-minecraft-extras", "1.7.1")
    implementation("com.zaxxer","HikariCP","5.0.1")
}

indra {
    javaVersions().target(17)
}

bukkit {
    main = "me.xanium.gemseconomy.GemsEconomy"
    name = project.name
    apiVersion = "1.17"
    authors = listOf("Xanium", "Nailm")
    depend = listOf("CommandAPI")
    softDepend = listOf("Vault")
    load = STARTUP
    loadBefore = listOf("ItemFrameShops")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        minimize()
        archiveFileName.set("${project.name}-${project.version}.jar")
        sequenceOf(
            "org.slf4j",
            "org.jetbrains",
            "com.zaxxer",
            "cloud.commandframework",
            "io.leangen.geantyref"
        ).forEach {
            relocate(it, "me.xanium.gemseconomy.lib.$it")
        }
    }
    processResources {
        val tokens = mapOf(
            "project.version" to project.version
        )
        inputs.properties(tokens)
    }
    task("deploy") {
        dependsOn(build)
        doLast {
            exec {
                workingDir("build/libs")
                commandLine("scp", jar.get().archiveFileName.get(), "dev:data/devmisc/plugins")
            }
        }
    }
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this