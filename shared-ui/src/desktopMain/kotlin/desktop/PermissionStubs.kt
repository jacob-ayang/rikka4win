package me.rerere.rikkahub.ui.components.ui.permission

import androidx.compose.runtime.Composable

class PermissionState(
    val permissions: Set<PermissionInfo>,
    val allPermissionsGranted: Boolean = true,
) {
    fun requestPermissions() = Unit
}

@Composable
fun rememberPermissionState(permissions: Set<PermissionInfo>): PermissionState {
    return PermissionState(permissions = permissions, allPermissionsGranted = true)
}

@Composable
fun PermissionManager(permissionState: PermissionState) = Unit

@Composable
fun PermissionNotification(permissionState: PermissionState) = Unit
