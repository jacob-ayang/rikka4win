package me.rerere.ai.provider

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.rerere.ai.ui.ImageGenerationResult
import me.rerere.ai.ui.MessageChunk
import me.rerere.ai.ui.UIMessage

class ProviderManager {
    suspend fun checkBalance(provider: ProviderSetting): String? = null

    fun getProviderByType(provider: ProviderSetting): Provider<ProviderSetting> {
        return object : Provider<ProviderSetting> {
            override suspend fun listModels(providerSetting: ProviderSetting): List<Model> = emptyList()

            override suspend fun generateText(
                providerSetting: ProviderSetting,
                messages: List<UIMessage>,
                params: TextGenerationParams,
            ): MessageChunk {
                return MessageChunk()
            }

            override suspend fun streamText(
                providerSetting: ProviderSetting,
                messages: List<UIMessage>,
                params: TextGenerationParams,
            ): Flow<MessageChunk> = flow { emit(MessageChunk()) }

            override suspend fun generateImage(
                providerSetting: ProviderSetting,
                params: ImageGenerationParams,
            ): ImageGenerationResult {
                return ImageGenerationResult(emptyList())
            }
        }
    }
}
