package me.rerere.rikkahub.ui.hooks

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun readBooleanPreference(key: String, defaultValue: Boolean = false): Boolean {
    val context = LocalContext.current
    return context.readBooleanPreference(key, defaultValue)
}

@Composable
fun readStringPreference(key: String, defaultValue: String? = null): String? {
    val context = LocalContext.current
    return context.readStringPreference(key, defaultValue)
}
