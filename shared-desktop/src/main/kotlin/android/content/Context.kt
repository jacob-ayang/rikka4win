package android.content

import java.io.File

open class Context(
    private val baseDir: File = File(System.getProperty("user.home"), ".rikkahub")
) {
    val filesDir: File = File(baseDir, "files").apply { mkdirs() }
    val cacheDir: File = File(baseDir, "cache").apply { mkdirs() }
    private val prefsDir: File = File(baseDir, "prefs").apply { mkdirs() }
    val contentResolver: ContentResolver = ContentResolver()
    val packageName: String = "me.rerere.rikkahub.desktop"

    open fun getDatabasePath(name: String): File {
        val dbDir = File(baseDir, "databases").apply { mkdirs() }
        return File(dbDir, name)
    }

    open fun getString(id: Int): String = "string:$id"

    open fun getString(id: Int, vararg args: Any?): String {
        return "string:$id " + args.joinToString(separator = " ")
    }

    open fun getApplicationContext(): Context = this

    fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        return FileBackedSharedPreferences(File(prefsDir, "$name.json"))
    }

    open fun startActivity(intent: Intent) {
        // No-op for desktop shim.
    }

    companion object {
        const val MODE_PRIVATE = 0
    }
}
