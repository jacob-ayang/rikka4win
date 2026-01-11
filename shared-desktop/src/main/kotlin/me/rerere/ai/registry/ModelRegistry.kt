package me.rerere.ai.registry

import me.rerere.ai.provider.ModelAbility
import me.rerere.ai.provider.Modality

object ModelRegistry {
    object GEMINI_SERIES {
        fun match(modelId: String): Boolean = modelId.contains("gemini", ignoreCase = true)
    }

    object MODEL_INPUT_MODALITIES {
        fun getData(modelId: String): List<Modality> = listOf(Modality.TEXT)
    }

    object MODEL_OUTPUT_MODALITIES {
        fun getData(modelId: String): List<Modality> = listOf(Modality.TEXT)
    }

    object MODEL_ABILITIES {
        fun getData(modelId: String): List<ModelAbility> = emptyList()
    }
}
