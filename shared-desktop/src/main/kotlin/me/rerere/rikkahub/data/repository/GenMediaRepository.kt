package me.rerere.rikkahub.data.repository

import androidx.paging.PagingSource
import me.rerere.rikkahub.data.db.entity.GenMediaEntity

class GenMediaRepository {
    private val items = mutableListOf<GenMediaEntity>()

    fun getAllMedia(): PagingSource<Int, GenMediaEntity> = object : PagingSource<Int, GenMediaEntity>() {
        override fun loadAll(): List<GenMediaEntity> = items.toList()
    }

    suspend fun insertMedia(media: GenMediaEntity) {
        items.add(media.copy(id = (items.maxOfOrNull { it.id } ?: 0) + 1))
    }

    suspend fun deleteMedia(id: Int) {
        items.removeAll { it.id == id }
    }
}
