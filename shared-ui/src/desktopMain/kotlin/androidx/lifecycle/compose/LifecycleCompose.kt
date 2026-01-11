package androidx.lifecycle.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(initialValue: T): State<T> {
    return collectAsState(initial = initialValue)
}

@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(): State<T> {
    return collectAsState()
}

@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(initialValue: T): State<T> {
    return collectAsState(initial = initialValue)
}
