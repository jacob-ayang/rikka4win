package me.rerere.rikkahub.data.repository

import androidx.paging.PagingSource

internal class ListPagingSource<T : Any>(private val items: List<T>) : PagingSource<Int, T>() {
    override fun loadAll(): List<T> = items
}
