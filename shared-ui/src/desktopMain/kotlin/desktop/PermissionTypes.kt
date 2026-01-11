package me.rerere.rikkahub.ui.components.ui.permission

import androidx.compose.runtime.Composable

data class PermissionInfo(
    val permission: String,
    val displayName: @Composable () -> Unit = {},
    val usage: @Composable () -> Unit = {},
    val required: Boolean = false
)

val PermissionNotification = PermissionInfo(permission = "android.permission.POST_NOTIFICATIONS")
