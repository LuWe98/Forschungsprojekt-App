package com.serverless.forschungsprojectfaas.extensions

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serverless.forschungsprojectfaas.OwnApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


inline fun ViewModel.launch(
    dispatcher: CoroutineContext = EmptyCoroutineContext,
    scope: CoroutineScope = viewModelScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    scope.launch(dispatcher) {
        block.invoke(this)
    }
}

val AndroidViewModel.app get() = getApplication<OwnApplication>()