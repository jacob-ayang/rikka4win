package org.koin.androidx.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition

@Composable
inline fun <reified T : ViewModel> koinViewModel(
    noinline parameters: ParametersDefinition? = null
): T {
    return GlobalContext.get().get(parameters = parameters)
}
