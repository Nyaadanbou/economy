import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    id("economy-conventions.commons")
    alias(libs.plugins.pluginyml.paper)
}

group = "cc.mewcraft.economy"
version = "2.2.2"
description = "The Bukkit plugin of Economy"

dependencies {
    // server
    compileOnly(local.paper)

    // internal
    implementation(project(":api"))
    implementation(project(":papi"))
    implementation(project(":mini"))
    implementation(local.redisson)
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
    serverDependencies {
        register("helper") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("Vault") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("ConnectorPlugin") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("PlaceholderAPI") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("MiniPlaceholders") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }

        // 用于覆盖 Essentials 的经济指令
        register("Essentials") {
            required = false
            joinClasspath = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}
