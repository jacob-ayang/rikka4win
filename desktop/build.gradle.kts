plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val desktopVersion = (rootProject.findProperty("rikkahub.version") as String?) ?: "1.7.8"

compose.desktop {
    application {
        mainClass = "me.rerere.rikkahub.desktop.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "RikkaHub"
            packageVersion = desktopVersion
        }
    }
}

dependencies {
    implementation(project(":backup-core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.dav4jvm)
    implementation(libs.sqlite.jdbc)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
}
