plugins {
    id("cc.mewcraft.project-conventions")
    alias(libs.plugins.indra)
}

// name and description inherited from "project-conventions"
version = "1.0.0"

dependencies {
    compileOnly(project(":bukkit"))

    // the server api
    compileOnly(libs.server.paper)

    // libs that present as other plugins
    compileOnly(libs.helper)
    compileOnly(libs.papi)
}

tasks {
    jar {
        archiveFileName.set("Expansion-${rootProject.name.lowercase()}.jar")
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
