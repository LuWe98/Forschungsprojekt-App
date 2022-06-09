package com.serverless.forschungsprojectfaas.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.DfAlertArgs
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VmAlert @Inject constructor(
    state: SavedStateHandle,
    private val navDispatcher: NavigationEventDispatcher
): ViewModel() {

    private val args = DfAlertArgs.fromSavedStateHandle(state)

    val parsedMessage get() = args.alertMessage


    fun onConfirmButtonClicked(){
        launch {
            navDispatcher.dispatch(NavigationEventDispatcher.NavigationEvent.NavigateBack)
        }
    }
}