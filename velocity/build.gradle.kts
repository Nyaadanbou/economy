plugins {
    id("cc.mewcraft.common")

    val indraVersion = "3.0.1"
    id("net.kyori.indra") version indraVersion
    id("net.kyori.indra.git") version indraVersion
}

version = "${rootProject.version}".decorateVersion()

dependencies {
    compileOnly("com.zaxxer", "HikariCP", "5.0.1")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.velocitypowered", "velocity-api", "3.2.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered", "velocity-api", "3.2.0-SNAPSHOT")
}

indra {
    javaVersions().target(17)
}

java {
    withSourcesJar()
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this
