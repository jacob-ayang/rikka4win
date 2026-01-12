package me.rerere.rikkahub.data.ai.tools

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import me.rerere.ai.core.InputSchema
import me.rerere.ai.core.Tool
import javax.script.ScriptEngineManager

@Serializable
sealed class LocalToolOption {
    @Serializable
    @SerialName("javascript_engine")
    data object JavascriptEngine : LocalToolOption()
}

class LocalTools {
    private val jsEngine by lazy { ScriptEngineManager().getEngineByName("JavaScript") }

    private val javascriptTool by lazy {
        Tool(
            name = "eval_javascript",
            description = "Execute JavaScript code with the local JS engine.",
            parameters = {
                InputSchema.Obj(
                    properties = buildJsonObject {
                        put("code", buildJsonObject {
                            put("type", "string")
                            put("description", "The JavaScript code to execute")
                        })
                    }
                )
            },
            execute = { args ->
                val code = args.jsonObject["code"]?.jsonPrimitive?.contentOrNull
                val engine = jsEngine
                val result = if (engine == null) {
                    "JavaScript engine not available"
                } else {
                    runCatching { engine.eval(code) }.getOrNull()?.toString() ?: ""
                }
                buildJsonObject { put("result", JsonPrimitive(result)) }
            }
        )
    }

    fun getTools(options: List<LocalToolOption>): List<Tool> {
        if (options.contains(LocalToolOption.JavascriptEngine) && jsEngine != null) {
            return listOf(javascriptTool)
        }
        return emptyList()
    }
}
