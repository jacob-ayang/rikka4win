package androidx.compose.material3

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class MenuAnchorType {
    PrimaryEditable,
    PrimaryNotEditable,
}

typealias ExposedDropdownMenuAnchorType = MenuAnchorType

fun Modifier.menuAnchor(type: MenuAnchorType): Modifier = this

@Composable
fun LinearWavyProgressIndicator(
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(modifier = modifier)
}

@Composable
fun CircularWavyProgressIndicator(
    modifier: Modifier = Modifier,
) {
    CircularProgressIndicator(modifier = modifier)
}

@Composable
fun LoadingIndicator() {
    CircularProgressIndicator()
}
