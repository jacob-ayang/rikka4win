package androidx.paging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class PagingConfig(
    val pageSize: Int,
    val initialLoadSize: Int = pageSize,
    val enablePlaceholders: Boolean = false,
)

open class PagingSource<Key : Any, Value : Any> {
    open fun loadAll(): List<Value> = emptyList()
}

class PagingData<T : Any>(val items: List<T>) {
    companion object {
        fun <T : Any> from(items: List<T>): PagingData<T> = PagingData(items)
    }
}

class Pager<Key : Any, Value : Any>(
    private val config: PagingConfig,
    private val pagingSourceFactory: () -> PagingSource<Key, Value>
) {
    val flow: Flow<PagingData<Value>> = flowOf(
        PagingData.from(pagingSourceFactory().loadAll())
    )
}

fun <T : Any, R : Any> PagingData<T>.map(transform: (T) -> R): PagingData<R> {
    return PagingData(items.map(transform))
}

fun <T : Any, R : Any> PagingData<T>.insertSeparators(
    generator: (before: T?, after: T?) -> R?
): PagingData<R> {
    if (items.isEmpty()) return PagingData(emptyList())
    val result = mutableListOf<R>()
    for (index in items.indices) {
        val before = if (index == 0) null else items[index - 1]
        val after = items[index]
        val separator = generator(before, after)
        if (separator != null) result.add(separator)
        @Suppress("UNCHECKED_CAST")
        result.add(after as R)
    }
    return PagingData(result)
}

fun <T : Any> Flow<PagingData<T>>.cachedIn(scope: CoroutineScope): Flow<PagingData<T>> = this
