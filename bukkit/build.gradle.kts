import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("cc.mewcraft.deploy-conventions")
    alias(libs.plugins.pluginyml.paper)
}

group = "me.xanium.gemseconomy"
version = "2.0.1"
description = "A modern multi-currency economy plugin on Bukkit"

project.ext.set("name", "GemsEconomy")

dependencies {
    // server
    compileOnly(libs.server.paper)

    // internal
    implementation(project(":economy:api"))
    implementation(project(":economy:papi"))
    implementation(project(":economy:mini"))
    implementation(project(":spatula:bukkit:command"))
    implementation(project(":spatula:bukkit:message"))
    implementation(libs.hikari)

    // standalone plugins
    compileOnly(libs.helper)
    compileOnly(libs.helper.sql)
    compileOnly(libs.helper.redis)
    compileOnly(libs.connector.core)
    compileOnly(libs.connector.bukkit)
    compileOnly(libs.vault) {
        exclude("org.bukkit")
    }
}

paper {
    main = "me.xanium.gemseconomy.GemsEconomyPlugin"
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
