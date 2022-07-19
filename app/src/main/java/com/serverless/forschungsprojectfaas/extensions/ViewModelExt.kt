package com.serverless.forschungsprojectfaas.extensions

import androidx.lifecycle.AndroidViewModel
import com.serverless.forschungsprojectfaas.ProjectApplication


//inline fun ViewModel.launch(
//    dispatcher: CoroutineContext = Dispatchers.IO,
//    scope: CoroutineScope = viewModelScope,
//    crossinline block: suspend CoroutineScope.() -> Unit
//) {
//    scope.launch(dispatcher) {
//        block.invoke(this)
//    }
//}

val AndroidViewModel.app get() = getApplication<ProjectApplication>()