plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation("io.insert-koin:koin-core")
    implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
    implementation(project(":ai-desktop"))
    implementation(project(":search-desktop"))
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.pebble)
    implementation(libs.commons.text)
    implementation(libs.dav4jvm)
    implementation("net.sf.kxml:kxml2:2.3.0")
    implementation(libs.modelcontextprotocol.kotlin.sdk)
    implementation(libs.sqlite.jdbc)
    implementation(libs.mp3spi)
    implementation(libs.pdfbox)
    implementation(libs.poi.ooxml)
    implementation(libs.zxing.core)
    implementation(libs.metadata.extractor)
    implementation(libs.org.json)
    implementation(kotlin("reflect"))
}
