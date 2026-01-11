package androidx.lifecycle

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

open class ViewModel {
    private val job = SupervisorJob()
    val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Default + job)

    protected open fun onCleared() {
        job.cancel()
    }
}

open class AndroidViewModel(private val application: Application) : ViewModel() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Application> getApplication(): T = application as T
}
