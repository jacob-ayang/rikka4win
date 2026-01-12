package me.rerere.rikkahub.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.rerere.rikkahub.data.db.DesktopDatabase
import me.rerere.rikkahub.data.db.entity.GenMediaEntity

class GenMediaRepository(
    private val database: DesktopDatabase,
) {
    private val items = MutableStateFlow(loadAllMedia())

    fun getAllMedia(): PagingSource<Int, GenMediaEntity> = ListPagingSource(items.value)

    suspend fun insertMedia(media: GenMediaEntity) {
        withContext(Dispatchers.IO) {
            database.query { conn ->
                conn.prepareStatement(
                    "INSERT INTO genmediaentity (path, model_id, prompt, create_at) VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
                ).use { statement ->
                    statement.setString(1, media.path)
                    statement.setString(2, media.modelId)
                    statement.setString(3, media.prompt)
                    statement.setLong(4, media.createAt)
                    statement.executeUpdate()
                }
            }
            refreshMedia()
        }
    }

    suspend fun deleteMedia(id: Int) {
        withContext(Dispatchers.IO) {
            database.query { conn ->
                conn.prepareStatement("DELETE FROM genmediaentity WHERE id = ?").use { statement ->
                    statement.setInt(1, id)
                    statement.executeUpdate()
                }
            }
            refreshMedia()
        }
    }

    private fun refreshMedia() {
        items.value = loadAllMedia()
    }

    private fun loadAllMedia(): List<GenMediaEntity> {
        return database.query { conn ->
            conn.prepareStatement(
                "SELECT id, path, model_id, prompt, create_at FROM genmediaentity ORDER BY id DESC"
            ).use { statement ->
                statement.executeQuery().use { rs ->
                    val results = mutableListOf<GenMediaEntity>()
                    while (rs.next()) {
                        results.add(
                            GenMediaEntity(
                                id = rs.getInt("id"),
                                path = rs.getString("path"),
                                modelId = rs.getString("model_id"),
                                prompt = rs.getString("prompt"),
                                createAt = rs.getLong("create_at")
                            )
                        )
                    }
                    results
                }
            }
        }
    }
}
