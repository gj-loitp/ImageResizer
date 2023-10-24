@file:Suppress("UnstableApiUsage")

include(":jxlcoder-coil")


pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}
rootProject.name = "Image Toolbox"
include(":app")
include(":cropper")
include(":dynamic_theme")
include(":colordetector")
include(":gesture")
include(":beforeafter")
include(":image")
include(":screenshot")
include(":modalsheet")
include(":gpuimage")
include(":colorpicker")
include(":systemuicontroller")
include(":placeholder")