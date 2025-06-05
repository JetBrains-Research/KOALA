pluginManagement {
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
}

rootProject.name = "koala"
include("ij-plugin")
include("ij-server")
include("processing")
