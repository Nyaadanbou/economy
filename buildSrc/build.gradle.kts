plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    nyaadanbouPrivate()
}

dependencies {
    implementation(local.plugin.kotlin.jvm)
    implementation(local.plugin.kotlin.kapt)
    implementation(local.plugin.shadow)
    implementation(local.plugin.nyaadanbou.conventions)
}

dependencies {
    implementation(files(local.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}