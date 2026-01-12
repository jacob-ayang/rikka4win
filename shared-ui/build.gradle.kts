import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("androidx.compose.animation.ExperimentalSharedTransitionApi")
        optIn.add("androidx.compose.foundation.ExperimentalFoundationApi")
        optIn.add("androidx.compose.foundation.layout.ExperimentalLayoutApi")
        optIn.add("kotlin.time.ExperimentalTime")
    }
    androidTarget()
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(project(":shared-android"))
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.lifecycle.process)
                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.profileinstaller)

                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.ui)
                implementation(libs.androidx.ui.graphics)
                implementation(libs.androidx.ui.tooling.preview)
                implementation(libs.androidx.material3)
                implementation(libs.androidx.material3.adaptive)
                implementation(libs.androidx.material3.adaptive.layout)

                implementation(libs.androidx.navigation2)

                implementation(libs.firebase.analytics)
                implementation(libs.firebase.crashlytics)
                implementation(libs.firebase.config)

                implementation(libs.androidx.datastore.preferences)

                implementation(libs.metadata.extractor)

                implementation(libs.koin.android)
                implementation(libs.koin.compose)
                implementation(libs.koin.androidx.workmanager)

                implementation(libs.jetbrains.markdown)

                implementation(libs.okhttp)
                implementation(libs.okhttp.sse)
                implementation(libs.retrofit)
                implementation(libs.retrofit.serialization.json)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(libs.ucrop)
                implementation(libs.pebble)
                implementation(libs.coil.compose)
                implementation(libs.coil.okhttp)
                implementation(libs.coil.svg)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.zxing.core)
                implementation(libs.quickie.bundled)
                implementation(libs.barcode.scanning)
                implementation(libs.androidx.camera.core)

                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.room.ktx)
                implementation(libs.androidx.room.paging)

                implementation(libs.androidx.paging.runtime)
                implementation(libs.androidx.paging.compose)

                implementation(libs.dav4jvm)

                implementation(libs.commons.text)
                implementation(libs.sonner)
                implementation(libs.reorderable)
                implementation(libs.lucide.icons)
                implementation(libs.image.viewer)
                implementation(libs.modelcontextprotocol.kotlin.sdk)

                implementation(project(":ai"))
                implementation(project(":document"))
                implementation(project(":highlight"))
                implementation(project(":search"))
                implementation(project(":tts"))
                implementation(project(":common"))
                implementation(kotlin("reflect"))
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":shared-desktop"))
                implementation(project(":ai-desktop"))
                implementation(project(":search-desktop"))
                implementation("io.insert-koin:koin-compose")
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.navigation2)
                implementation(libs.jetbrains.markdown)
                implementation(libs.reorderable)
                implementation(libs.lucide.icons)
                implementation(libs.sonner)
                implementation(libs.jsoup)
                implementation(libs.modelcontextprotocol.kotlin.sdk)
                implementation(libs.kotlinx.datetime)
                implementation(libs.okhttp)
                implementation(kotlin("reflect"))
            }
            kotlin.srcDir("src/androidMain/kotlin")
            kotlin.exclude(
                "**/*.android.kt",
                "**/ui/activity/**",
                "**/ui/components/webview/**",
                "**/ui/pages/webview/**",
                "**/ui/components/ui/permission/**",
                "**/ui/components/ui/QRCode.kt",
                "**/ui/components/ui/BitmapComposer.kt",
                "**/ui/components/ui/Emoji.kt",
                "**/ui/components/ui/FloatingWindow.kt",
                "**/ui/components/ui/ImagePreviewDialog.kt",
                "**/ui/components/ui/LoadingBlock.kt",
                "**/ui/components/ui/TextArea.kt",
                "**/ui/components/ui/Tooltip.kt",
                "**/ui/components/ui/UIAvatar.kt",
                "**/ui/components/ui/UpdateCard.kt",
                "**/ui/components/ui/ViewText.kt",
                "**/ui/components/richtext/LatexText.kt",
                "**/ui/components/richtext/Mermaid.kt",
                "**/ui/components/richtext/MarkdownWeb.kt",
                "**/ui/components/richtext/ZoomableAsyncImage.kt",
                "**/ui/pages/chat/Export.kt",
                "**/ui/pages/assistant/detail/AssistantImporter.kt",
                "**/ui/components/ai/ChatInput.kt",
                "**/ui/hooks/AvatarShape.kt",
                "**/ui/hooks/HeroAnimation.kt",
                "**/ui/hooks/Lifecycle.kt",
                "**/ui/hooks/PlayStore.kt",
                "**/ui/hooks/TTS.kt",
            )
            resources.srcDirs("src/androidMain/res", "../app/src/main/res")
        }
    }
}

android {
    namespace = "me.rerere.rikkahub"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].res.srcDirs("src/androidMain/res", "../app/src/main/res")
}

dependencies {
    add("androidMainImplementation", platform(libs.androidx.compose.bom))
    add("androidMainImplementation", platform(libs.firebase.bom))
    add("androidMainImplementation", platform(libs.koin.bom))
    add("desktopMainImplementation", platform(libs.koin.bom))
}
