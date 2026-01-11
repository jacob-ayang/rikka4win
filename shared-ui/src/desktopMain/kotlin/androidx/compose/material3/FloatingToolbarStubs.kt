package androidx.compose.material3

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection

object FloatingToolbarDefaults {
    val ScreenOffset: Dp = 24.dp

    fun floatingToolbarVerticalNestedScroll(): NestedScrollConnection {
        return object : NestedScrollConnection {}
    }

    fun Modifier.floatingToolbarVerticalNestedScroll(
        expanded: Boolean,
        onExpand: () -> Unit,
        onCollapse: () -> Unit,
    ): Modifier = this
}

@Composable
fun HorizontalFloatingToolbar(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Row(modifier = modifier) {
        leadingContent?.invoke()
        content()
    }
}
