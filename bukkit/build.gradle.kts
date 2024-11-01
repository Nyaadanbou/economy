import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("economy-conventions.commons")
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    alias(libs.plugins.pluginyml.paper)
}

group = "cc.mewcraft.economy"
version = "2.0.1"
description = "A modern multi-currency economy plugin on Bukkit"

project.ext.set("name", "economy")

dependencies {
    // server
    compileOnly(libs.server.paper)

    // internal
    implementation(project(":api"))
    implementation(project(":papi"))
    implementation(project(":mini"))
    implementation(libs.cloud2.core)
    implementation(libs.cloud2.paper)
    implementation(libs.cloud2.minecraft.extras)
    implementation(local.lang.bukkit)
    implementation(libs.hikari)

    // standalone plugins
    compileOnly(local.helper)
    compileOnly(libs.helper.sql)
    compileOnly(libs.helper.redis)
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
    name = project.ext.get("name") as String
    version = "${project.version}"
    description = project.description
    apiVersion = "1.19"
    authors = listOf("Nailm")
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
