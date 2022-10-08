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
    maven("https://repo.papermc.io/repository/maven-public/") {
        content {
            includeGroup("io.papermc.paper")
        }
    }
    maven("https://jitpack.io") {
        content {
            includeGroup("com.github.MilkBowl")
        }
    }
    maven("https://repo.minebench.de") {
        content {
            includeGroup("de.themoep.utils")
        }
    }
}

dependencies {
    // API
    compileOnly("io.papermc.paper", "paper-api", "1.17.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains", "annotations", "23.0.0")

    // Plugin libraries
    compileOnly("me.lucko", "helper", "5.6.10")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")

    // Libraries that needs to be shaded
    implementation("net.kyori", "adventure-api", "4.11.0")
    implementation("net.kyori", "adventure-platform-bukkit", "4.1.2")
    implementation("net.kyori", "adventure-text-minimessage", "4.11.0")
    implementation("de.themoep.utils", "lang-bukkit", "1.3-SNAPSHOT")
    implementation("com.zaxxer", "HikariCP", "5.0.1")
    val cloudVersion = "1.7.1"
    implementation("cloud.commandframework", "cloud-paper", cloudVersion)
    implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)

    annotationProcessor("cloud.commandframework:cloud-annotations:1.7.1")
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
//            "org.slf4j",
//            "org.jetbrains",
            "net.kyori",
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
                commandLine("scp", jar.get().archiveFileName.get(), "dev:data/dev/plugins")
            }
        }
    }
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this