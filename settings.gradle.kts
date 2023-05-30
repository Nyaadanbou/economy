rootProject.name = "GemsEconomy"

include(":velocity")
include(":bukkit")
include(":papi")

// import common settings.gradle of Mewcraft projects
apply(from = "${System.getenv("HOME")}/MewcraftGradle/mirrors.settings.gradle.kts")
