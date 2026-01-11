package androidx.paging.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class LazyPagingItems<T : Any>(private val data: PagingData<T>) {
    val itemCount: Int get() = data.items.size
    operator fun get(index: Int): T? = data.items.getOrNull(index)

    fun refresh() = Unit
}

@Composable
fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(): LazyPagingItems<T> {
    val state by collectAsState(initial = PagingData.from(emptyList<T>()))
    return LazyPagingItems(state)
}

fun <T : Any> LazyPagingItems<T>.itemKey(key: (T) -> Any): (index: Int) -> Any = { index ->
    get(index)?.let(key) ?: index
}

fun <T : Any> LazyPagingItems<T>.itemContentType(type: (T) -> Any?): (index: Int) -> Any = { index ->
    get(index)?.let(type) ?: Unit
}
