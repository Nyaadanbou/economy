plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(uri("${System.getenv("HOME")}/MewcraftRepository"))
}

dependencies {
    implementation("cc.mewcraft.java-conventions", "cc.mewcraft.java-conventions.gradle.plugin", "1.0.0")
    implementation("cc.mewcraft.publishing-conventions", "cc.mewcraft.publishing-conventions.gradle.plugin", "1.0.0")
    implementation("cc.mewcraft.repository-conventions", "cc.mewcraft.repository-conventions.gradle.plugin", "1.0.0")
}
