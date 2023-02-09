plugins {
    id("cc.mewcraft.common")

    val indraVersion = "3.0.1"
    id("net.kyori.indra") version indraVersion
    id("net.kyori.indra.git") version indraVersion

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

version = "1.3.3".decorateVersion()
description = "A multi-currency economy plugin for spigot servers"

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this

dependencies {
    compileOnly("com.zaxxer", "HikariCP", "5.0.1")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.velocitypowered", "velocity-api", "3.2.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered", "velocity-api", "3.2.0-SNAPSHOT")
}

tasks {
    jar {
        archiveClassifier.set("nonshade")
    }
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        minimize()
        archiveFileName.set("${rootProject.name}-${project.version}.jar")
        archiveClassifier.set("")
        sequenceOf(
            ""
        ).forEach {
            relocate(it, "me.xanium.gemseconomy.shade.$it")
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
            artifactId = rootProject.name
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
