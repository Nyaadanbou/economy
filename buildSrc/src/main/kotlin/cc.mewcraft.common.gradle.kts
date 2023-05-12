plugins {
    `java-library`
    `maven-publish`
}

group = "me.xanium.gemseconomy"
version = "1.3.8"
description = "A modern multi-currency economy plugin"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        content {
            includeGroup("io.papermc.paper")
            includeGroup("com.velocitypowered")
            includeGroup("net.md-5") // contains project: bungeecord-chat-1.16-R0.4-deprecated
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
            includeGroup("de.themoep.connectorplugin")
        }
    }
    maven("'https://repo.extendedclip.com/content/repositories/placeholderapi/'") {
        content {
            includeGroup("me.clip")
        }
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}