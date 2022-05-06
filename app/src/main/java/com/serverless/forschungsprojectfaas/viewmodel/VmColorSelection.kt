package com.serverless.forschungsprojectfaas.viewmodel

import android.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.*
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.BsdfColorSelectionArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class VmColorSelection @Inject constructor(
    private val navDispatcher: NavigationEventDispatcher,
    state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfColorSelectionArgs.fromSavedStateHandle(state)

//    private colors ...

    private val redColorMutableStateFlow = MutableStateFlow(0)

    private val greenColorMutableStateFlow = MutableStateFlow(0)

    private val blueColorMutableStateFlow = MutableStateFlow(0)

    private val alphaColorMutableStateFlow = MutableStateFlow(0)

    val colorValueFlow = combine(
        flow = redColorMutableStateFlow,
        flow2 = greenColorMutableStateFlow,
        flow3 = blueColorMutableStateFlow,
        flow4 = alphaColorMutableStateFlow
    ) { red, green, blue, alpha ->
        Color.argb(alpha, red, green, blue)
    }

    fun onValidCircleTouchReceived(red: Int, green: Int, blue: Int) {
        redColorMutableStateFlow.value = red
        greenColorMutableStateFlow.value = green
        blueColorMutableStateFlow.value = blue
    }

    fun onSeekBarChanged(progress: Int, isUserInput: Boolean){

    }

    fun onCancelButtonClicked(){
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }
}