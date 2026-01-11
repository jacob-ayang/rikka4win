plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val desktopVersion = (rootProject.findProperty("rikkahub.version") as String?) ?: "0.0.1"

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
    implementation(project(":shared-ui"))
    implementation(compose.desktop.currentOs)
    implementation(platform(libs.koin.bom))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-compose")
}
