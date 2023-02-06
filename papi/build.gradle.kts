plugins {
    id("cc.mewcraft.common")

    val indraVersion = "3.0.1"
    id("net.kyori.indra") version indraVersion
    id("net.kyori.indra.git") version indraVersion
}

version = "1.0".decorateVersion()

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this-${lastCommitHash()}" else this

dependencies {
    compileOnly(project(":plugin"))
    compileOnly("me.lucko", "helper", "5.6.13")
    compileOnly("me.clip", "placeholderapi", "2.11.2")
    compileOnly("io.papermc.paper", "paper-api", "1.17.1-R0.1-SNAPSHOT")
}

tasks {
    jar {
        archiveFileName.set("Expansion-${project.name}.jar")
        archiveClassifier.set("")
    }
    task("deployToServer") {
        dependsOn(build)
        doLast {
            exec {
                commandLine("rsync", "${jar.get().archiveFile.get()}", "dev:data/dev/jar")
            }
        }
    }
}

indra {
    javaVersions().target(17)
}

java {
    withSourcesJar()
}