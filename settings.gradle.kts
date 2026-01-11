pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.itextsupport.com/android")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "io.objectbox") {
                useModule("io.objectbox:objectbox-gradle-plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
        mavenLocal()
    }
}

rootProject.name = "rikkahub"
include(":app")
include(":highlight")
include(":ai")
include(":search")
include(":tts")
include(":common")
include(":document")
include(":backup-core")
include(":desktop")
