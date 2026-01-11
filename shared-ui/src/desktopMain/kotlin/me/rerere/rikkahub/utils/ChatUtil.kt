package me.rerere.rikkahub.utils

import androidx.navigation.NavHostController
import me.rerere.rikkahub.Screen
import kotlin.uuid.Uuid

fun navigateToChatPage(
    navController: NavHostController,
    chatId: Uuid = Uuid.random(),
    initText: String? = null,
    initFiles: List<android.net.Uri> = emptyList(),
) {
    navController.navigate(
        route = Screen.Chat(
            id = chatId.toString(),
            text = initText,
            files = initFiles.map { it.toString() },
        ),
    )
}
