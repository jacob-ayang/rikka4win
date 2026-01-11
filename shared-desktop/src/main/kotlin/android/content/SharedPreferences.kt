package android.content

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

interface SharedPreferences {
    fun getString(key: String, defValue: String?): String?
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun contains(key: String): Boolean
    fun edit(): Editor
    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener)
    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener)

    fun interface OnSharedPreferenceChangeListener {
        fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String)
    }

    interface Editor {
        fun putString(key: String, value: String?): Editor
        fun putBoolean(key: String, value: Boolean): Editor
        fun remove(key: String): Editor
        fun apply()
    }
}

class FileBackedSharedPreferences(private val file: File) : SharedPreferences {
    private val listeners = CopyOnWriteArrayList<SharedPreferences.OnSharedPreferenceChangeListener>()
    private val json = Json { ignoreUnknownKeys = true }

    private fun load(): MutableMap<String, String> {
        if (!file.exists()) return mutableMapOf()
        return try {
            val element = json.parseToJsonElement(file.readText())
            element.jsonObject.mapValues { it.value.jsonPrimitive.content }.toMutableMap()
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun save(values: Map<String, String>) {
        file.parentFile?.mkdirs()
        val content = buildString {
            append("{")
            values.entries.forEachIndexed { index, entry ->
                if (index > 0) append(",")
                append("\"").append(entry.key).append("\":")
                append("\"").append(entry.value.replace("\"", "\\\"")).append("\"")
            }
            append("}")
        }
        file.writeText(content)
    }

    override fun getString(key: String, defValue: String?): String? {
        return load()[key] ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return load()[key]?.toBooleanStrictOrNull() ?: defValue
    }

    override fun contains(key: String): Boolean = load().containsKey(key)

    override fun edit(): SharedPreferences.Editor = EditorImpl()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        listeners.add(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        listeners.remove(listener)
    }

    private inner class EditorImpl : SharedPreferences.Editor {
        private val values = load()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value == null) values.remove(key) else values[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            values[key] = value.toString()
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            values.remove(key)
            return this
        }

        override fun apply() {
            save(values)
            listeners.forEach { it.onSharedPreferenceChanged(this@FileBackedSharedPreferences, "") }
        }
    }
}
