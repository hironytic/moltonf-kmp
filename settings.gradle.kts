rootProject.name = "moltonf-kmp"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // Google's Maven Central mirror, listed first to avoid "429 Too Many Requests" errors from Maven Central
        // (see https://central.sonatype.org/faq/429-error/). Anything missing here falls through to the repositories below.
        maven("https://maven-central.storage.googleapis.com/maven2/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // Google's Maven Central mirror, listed first to avoid "429 Too Many Requests" errors from Maven Central
        // (see https://central.sonatype.org/faq/429-error/). Anything missing here falls through to the repositories below.
        maven("https://maven-central.storage.googleapis.com/maven2/")
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":desktopApp")
include(":shared")