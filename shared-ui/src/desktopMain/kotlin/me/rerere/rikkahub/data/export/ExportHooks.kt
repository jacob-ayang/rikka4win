package me.rerere.rikkahub.data.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
class ExporterState<T>(
    private val data: T,
    private val serializer: ExportSerializer<T>,
) {
    val value: String
        get() = serializer.exportToJson(data)

    val fileName: String
        get() = serializer.getExportFileName(data)

    fun exportToFile(fileName: String = this.fileName) = Unit

    fun exportAndShare(fileName: String = this.fileName) = Unit
}

@Composable
fun <T> rememberExporter(
    data: T,
    serializer: ExportSerializer<T>,
): ExporterState<T> {
    return remember(data, serializer) {
        ExporterState(data = data, serializer = serializer)
    }
}

@Stable
class ImporterState<T>(
    private val serializer: ExportSerializer<T>,
    private val onResult: (Result<T>) -> Unit,
) {
    fun importFromFile() {
        onResult(Result.failure(UnsupportedOperationException("Import not supported on desktop yet.")))
    }
}

@Composable
fun <T> rememberImporter(
    serializer: ExportSerializer<T>,
    onResult: (Result<T>) -> Unit,
): ImporterState<T> {
    return remember(serializer) {
        ImporterState(serializer = serializer, onResult = onResult)
    }
}
