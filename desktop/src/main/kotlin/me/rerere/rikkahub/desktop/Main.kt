package me.rerere.rikkahub.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import me.rerere.rikkahub.sharedui.AppRoot
import me.rerere.rikkahub.ui.theme.ColorMode
import me.rerere.rikkahub.ui.theme.ThemeSettings

fun main() {
    initDebugLogger()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "RikkaHub",
        ) {
            val themeSettings = ThemeSettings(
                themeId = "sakura",
                dynamicColor = false,
                amoledDarkMode = false,
                colorMode = ColorMode.SYSTEM,
            )
            AppRoot(themeSettings)
        }
    }
}

private fun initDebugLogger() {
    val logPath = Paths.get("rikkahub-debug.log").toAbsolutePath()
    val fileStream = Files.newOutputStream(
        logPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND,
    )
    val originalOut = System.out
    val originalErr = System.err
    val teeOut = PrintStream(TeeOutputStream(originalOut, fileStream), true, StandardCharsets.UTF_8.name())
    val teeErr = PrintStream(TeeOutputStream(originalErr, fileStream), true, StandardCharsets.UTF_8.name())
    System.setOut(teeOut)
    System.setErr(teeErr)
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        System.err.println("Uncaught exception in thread: ${thread.name}")
        throwable.printStackTrace()
    }
    System.out.println("RikkaHub debug log: $logPath")
    System.out.println("Working directory: ${System.getProperty("user.dir")}")
}

private class TeeOutputStream(
    private val first: OutputStream,
    private val second: OutputStream,
) : OutputStream() {
    override fun write(b: Int) {
        first.write(b)
        second.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        first.write(b, off, len)
        second.write(b, off, len)
    }

    override fun flush() {
        first.flush()
        second.flush()
    }
}
