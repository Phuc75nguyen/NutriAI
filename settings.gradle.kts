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
        // (không bắt buộc cho plugins, nhưng thêm cũng không hại)
        maven(url = "https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // *** QUAN TRỌNG: thêm JitPack cho dependencies ***
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "NutriAI"
include(":app")

