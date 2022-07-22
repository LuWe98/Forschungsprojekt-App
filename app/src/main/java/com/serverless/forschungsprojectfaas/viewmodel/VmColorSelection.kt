package com.serverless.forschungsprojectfaas.viewmodel

import android.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.*
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.*
import com.serverless.forschungsprojectfaas.extensions.toHex
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.BsdfColorSelectionArgs
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class VmColorSelection @Inject constructor(
    private val navDispatcher: NavigationEventDispatcher,
    private val resultDispatcher: FragmentResultDispatcher,
    state: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val HEX_BASE = 16
    }

    private val args = BsdfColorSelectionArgs.fromSavedStateHandle(state)

    val currentColorValue get() = args.currentColor


    private val redColorMutableStateFlow = MutableStateFlow(Color.red(currentColorValue))

    private val greenColorMutableStateFlow = MutableStateFlow(Color.green(currentColorValue))

    private val blueColorMutableStateFlow = MutableStateFlow(Color.blue(currentColorValue))

    private val alphaColorMutableStateFlow = MutableStateFlow(Color.alpha(currentColorValue))

    val colorValueFlow = combine(
        flow = redColorMutableStateFlow,
        flow2 = greenColorMutableStateFlow,
        flow3 = blueColorMutableStateFlow,
        flow4 = alphaColorMutableStateFlow
    ) { red, green, blue, alpha ->
        Color.argb(alpha, red, green, blue)
    }

    val hexColorValueFlow = combine(
        flow = redColorMutableStateFlow,
        flow2 = greenColorMutableStateFlow,
        flow3 = blueColorMutableStateFlow,
        flow4 = alphaColorMutableStateFlow
    ) { red, green, blue, alpha ->
        val redHex = red.toHex.let { if(red < HEX_BASE) "0$it" else it }
        val greenHex = green.toHex.let { if(green < HEX_BASE) "0$it" else it }
        val blueHex = blue.toHex.let { if(blue < HEX_BASE) "0$it" else it }
        val alphaHex = alpha.toHex.let { if(alpha < HEX_BASE) "0$it" else it }
        alphaHex + redHex + greenHex + blueHex
    }

    fun onValidCircleTouchReceived(red: Int, green: Int, blue: Int) {
        redColorMutableStateFlow.value = red
        greenColorMutableStateFlow.value = green
        blueColorMutableStateFlow.value = blue
    }

    fun onSeekBarChanged(progress: Int, isUserInput: Boolean, type: ColorType) {
        if (!isUserInput) return

        when (type) {
            ColorType.RED -> redColorMutableStateFlow.value = progress
            ColorType.GREEN -> greenColorMutableStateFlow.value = progress
            ColorType.BLUE -> blueColorMutableStateFlow.value = progress
            ColorType.ALPHA -> alphaColorMutableStateFlow.value = progress
        }
    }

    fun onCancelButtonClicked() {
        launch {
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }

    fun onSaveButtonClicked() {
        launch {
            resultDispatcher.dispatch(FragmentResult.ColorPickerResult(colorValueFlow.first()))
            navDispatcher.dispatch(NavigationEvent.NavigateBack)
        }
    }

    enum class ColorType {
        RED,
        GREEN,
        BLUE,
        ALPHA
    }
}