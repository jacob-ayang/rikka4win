package androidx.compose.ui.res

import androidx.compose.runtime.Composable

@Composable
fun stringResource(id: Int, vararg args: Any): String {
    return if (args.isNotEmpty()) "string:$id".format(*args) else "string:$id"
}
