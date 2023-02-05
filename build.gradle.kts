import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("net.kyori.indra") version "2.1.1"
    id("net.kyori.indra.git") version "2.1.1"
}

group = "me.xanium.gemseconomy"
version = "1.3.3".decorateVersion()
description = "A multi-currency economy plugin for spigot servers"

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this

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
    // Server API
    compileOnly("io.papermc.paper", "paper-api", "1.17.1-R0.1-SNAPSHOT")

    // Will be downloaded upon plugin startup
    compileOnly("com.zaxxer", "HikariCP", "5.0.1")

    // 3rd party plugins
    compileOnly("me.lucko", "helper", "5.6.13")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7") { isTransitive = false }

    // Libraries that needs to be shaded
    implementation("net.kyori", "adventure-api", "4.12.0")
    implementation("net.kyori", "adventure-text-minimessage", "4.12.0")
    implementation("net.kyori", "adventure-platform-bukkit", "4.1.2")
    implementation("de.themoep.utils", "lang-bukkit", "1.3-SNAPSHOT")
    val cloudVersion = "1.8.0"
    implementation("cloud.commandframework", "cloud-paper", cloudVersion)
    implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)
}

indra {
    javaVersions().target(17)
}

bukkit {
    main = "me.xanium.gemseconomy.GemsEconomy"
    name = project.name
    version = "${project.version}"
    apiVersion = "1.17"
    authors = listOf("Xanium", "Nailm")
    depend = listOf("helper")
    softDepend = listOf("Vault")
    load = STARTUP
    loadBefore = listOf("ItemFrameShops")
    libraries = listOf("com.zaxxer:HikariCP:5.0.1")
}

tasks {
    jar {
        enabled = false
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        minimize()
        archiveFileName.set("${project.name}-${project.version}.jar")
        archiveClassifier.set("")
        sequenceOf(
            "net.kyori",
            "cloud.commandframework",
            "io.leangen.geantyref",
            "de.themoep.utils",
            "org.apiguardian",
            "org.checkerframework",
            "org.jetbrains",
            "org.intellij",
            "org.slf4j"
        ).forEach {
            relocate(it, "me.xanium.gemseconomy.lib.$it")
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
            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["shadowJar"])
        }
    }
}
