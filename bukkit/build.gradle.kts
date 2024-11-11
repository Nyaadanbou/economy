import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    id("economy-conventions.commons")
    alias(libs.plugins.pluginyml.paper)
}

group = "cc.mewcraft.economy"
version = "2.0.1"
description = "The Bukkit plugin of Economy"

dependencies {
    // server
    compileOnly(local.paper)

    // internal
    implementation(project(":api"))
    implementation(project(":papi"))
    implementation(project(":mini"))
    implementation(local.lang.bukkit)
    implementation(libs.cloud2.core)
    implementation(libs.cloud2.paper)
    implementation(libs.cloud2.minecraft.extras)
    implementation(libs.hikari)

    // standalone plugins
    compileOnly(local.helper)
    compileOnly(local.helper.sql)
    compileOnly(local.helper.redis)
    compileOnly(libs.connector.core)
    compileOnly(libs.connector.bukkit)
    compileOnly(libs.vault) {
        exclude("org.bukkit")
    }
}

tasks {
    copyJar {
        environment = "paper"
        jarFileName = "economy-${project.version}.jar"
    }
}

paper {
    main = "cc.mewcraft.economy.EconomyPlugin"
    name = "Economy"
    version = "${project.version}"
    description = project.description
    apiVersion = "1.19"
    authors = listOf("Nailm")
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    serverDependencies {
        register("helper") {
            required = true
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("Vault") {
            required = false
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("ConnectorPlugin") {
            required = false
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("PlaceholderAPI") {
            required = false
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("MiniPlaceholders") {
            required = false
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
    }
}
