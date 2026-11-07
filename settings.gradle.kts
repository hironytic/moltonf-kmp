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
        // In Claude Code's cloud environment (CLAUDE_CODE_REMOTE=true), use Google's Maven Central mirror, listed first,
        // to avoid frequent "429 Too Many Requests" errors from Maven Central. These errors do not occur in typical
        // local environments, probably because the cloud egress network is shared with other users
        // (see https://central.sonatype.org/faq/429-error/).
        // Anything missing from the mirror falls through to the repositories below.
        if (providers.environmentVariable("CLAUDE_CODE_REMOTE").orNull == "true") {
            maven("https://maven-central.storage.googleapis.com/maven2/")
        }
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
        // In Claude Code's cloud environment (CLAUDE_CODE_REMOTE=true), use Google's Maven Central mirror, listed first,
        // to avoid frequent "429 Too Many Requests" errors from Maven Central. These errors do not occur in typical
        // local environments, probably because the cloud egress network is shared with other users
        // (see https://central.sonatype.org/faq/429-error/).
        // Anything missing from the mirror falls through to the repositories below.
        if (providers.environmentVariable("CLAUDE_CODE_REMOTE").orNull == "true") {
            maven("https://maven-central.storage.googleapis.com/maven2/")
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":desktopApp")
include(":shared")