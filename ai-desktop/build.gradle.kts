plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlin.time.ExperimentalTime")
    }
    sourceSets.main {
        kotlin.srcDir("../ai/src/main/java")
        kotlin.srcDir("../common/src/main/java/me/rerere/common/http")
        kotlin.exclude(
            "**/FileEncoder.kt",
            "**/AcceptLang.kt"
        )
    }
}

dependencies {
    implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
    api(libs.okhttp)
    api(libs.okhttp.sse)
    api(libs.okhttp.logging)
    implementation(libs.commons.text)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
}
